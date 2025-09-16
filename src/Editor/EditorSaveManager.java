
package Editor;

import ElectronicsBackend.ElectricalComponent;

import javax.tools.Diagnostic;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class EditorSaveManager {

    EditorArea editor;

    public EditorSaveManager(EditorArea editorArea) {
        editor = editorArea;
    }

    private String getEditorDataAsWritable() {
        StringBuilder content = new StringBuilder();
        for (Component c : editor.getComponents()){
            if (c instanceof DraggableEditorComponent dec) {
                content.append(dec.getElectricalComponent().toString());
                content.append("\n");
                content.append("ElectricalProperties("+dec.getElectricalComponent().electricalProperties.toString()+")");
                content.append("\n");
            }
        }
        for (Wire w : editor.wires) {
            content.append(w.toString());
            content.append("\n");
        }
        return content.toString();
    }



    public static String compress(String str) throws IOException {
        if (str == null || str.isEmpty()) return str;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        return out.toString(StandardCharsets.ISO_8859_1);
    }

    public HashMap<String, Object> loadHashFromMemory(String input) throws Exception {
        HashMap<String, Object> result = new HashMap<>();

        input = input.trim();
        if (input.startsWith("{")) input = input.substring(1);
        if (input.endsWith("}")) input = input.substring(0, input.length() - 1);

        ArrayList<String> pairs = new ArrayList();
        int braceCount = 0;
        StringBuilder current = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '{') braceCount++;
            if (c == '}') braceCount--;
            if (c == ',' && braceCount == 0) {
                pairs.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) pairs.add(current.toString());

        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length != 2) continue;

            String key = kv[0].trim();
            String valueStr = kv[1].trim();

            Object value = parseValue(valueStr);
            result.put(key, value);
        }

        return result;
    }

    private Object parseValue(String valueStr) throws Exception {
        if ("null".equalsIgnoreCase(valueStr)) return null;
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException ignored) {}
        if ("true".equalsIgnoreCase(valueStr)) return true;
        if ("false".equalsIgnoreCase(valueStr)) return false;
        if (valueStr.startsWith("{") && valueStr.endsWith("}")) {
            return loadHashFromMemory(valueStr);
        }
        return valueStr.replaceAll("^\"|\"$", "");
    }

    public static String decompress(String compressed) throws IOException {
        if (compressed == null || compressed.isEmpty()) return compressed;

        byte[] compressedBytes = compressed.getBytes(StandardCharsets.ISO_8859_1);

        ByteArrayInputStream in = new ByteArrayInputStream(compressedBytes);
        GZIPInputStream gzip = new GZIPInputStream(in);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzip.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        gzip.close();
        return out.toString(StandardCharsets.UTF_8);
    }


    public String getDataFromFile(File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()));
        return decompress(content);
    }

    public void loadFileToEditor(File file) {
        editor.reset();
        String data;
        try {data = getDataFromFile(file);}
        catch (IOException e) {throw new RuntimeException(e);}
        System.out.println(data);
        Pattern pattern = Pattern.compile("(?:.*?\\:(.*?)\\||.*?(\\[.*?\\])\\))");

        String[] components = data.split("\n");
        ArrayList<int[]> childrenList = new ArrayList<>();
        DraggableEditorComponent lastLoadedComponent = null;
        for (String c : components) {
            if (c.startsWith("ElectricalComponent")) {
                Matcher matcher = pattern.matcher(c);
                matcher.find();
                int compId = Integer.parseInt(matcher.group(1));
                matcher.find();
                String type = matcher.group(1);
                matcher.find();
                double x = Double.parseDouble(matcher.group(1));
                matcher.find();
                double y = Double.parseDouble(matcher.group(1));
                matcher.find();
                boolean isDead = Boolean.parseBoolean(matcher.group(1));
                matcher.find();
                int rot = Integer.parseInt(matcher.group(1));
                matcher.find();
                int[] children = parseIntegerArray(matcher.group(2));
                childrenList.add(children);

                try {
                    DraggableEditorComponent component = editor.createNewComponent(type, x, y, false);
                    component.orientation = rot;
                    component.getElectricalComponent().rotateConnectionPoints(rot);
                    lastLoadedComponent = component;

                } catch (Exception ignored){
                    ignored.printStackTrace();
                };
            }
            else if (c.startsWith("ElectricalProperties")) {
                try {
                    assert lastLoadedComponent != null;
                    String input = c.substring("ElectricalProperties(".length(),c.length()-1);
                    ElectricalComponent lastLoadedElectricalComponent = lastLoadedComponent.getElectricalComponent();
                    lastLoadedElectricalComponent.electricalProperties = loadHashFromMemory(input);
                    lastLoadedElectricalComponent.updateInfoPanelFromProperties();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
        //add children after all are loaded (prevents lookup on components that are not loaded yet)
        for (int i=0; i<childrenList.size(); i++) {
            int[] arr = childrenList.get(i);
            ElectricalComponent eC = ElectricalComponent.allComponents.get(i);
            for (int o : arr) {
                if (ElectricalComponent.allComponents.contains(o))
                    eC.children.add(ElectricalComponent.allComponents.get(o));
            }
        }
        editor.creatingComponentID = null;

        //add all wires in
        Pattern wirepat = Pattern.compile("(?:.*?\\:(.*?)\\||.*?\\:(.*?)\\))");
        for (String c : components) {
            if (c.startsWith("Wire")) {
                Matcher matcher = wirepat.matcher(c);
                matcher.find();
                ElectricalComponent a =
                        ElectricalComponent.allComponents.get(Integer.parseInt(matcher.group(1)));
                matcher.find();
                ElectricalComponent b =
                        ElectricalComponent.allComponents.get(Integer.parseInt(matcher.group(1)));
                matcher.find();
                int aStart = Integer.parseInt(matcher.group(1));
                matcher.find();
                int bStart = Integer.parseInt(matcher.group(2));
                editor.connectComponents(a, aStart, b, bStart, false);
            }
            //add properties to components after they all are loaded

        }
        editor.mainWindow.setTitle("WireWorks V1.0 - "+editor.currentlyEditingFile.getName());
        editor.zoomFit();
        editor.repaint();
    }

    private int[] parseIntegerArray(String strArray) {
        strArray = strArray.replace("[","")
                .replace("]","");

        String[] dataArr;
        if (!strArray.isEmpty()) dataArr = strArray.split(",");
        else dataArr = new String[]{};

        int[] output = new int[dataArr.length];
        for (int i = 0; i<dataArr.length; i++) {
            output[i] = Integer.parseInt(dataArr[i]);
        }
        return output;
    }

    public void saveFile(String filePath) {
        String content = getEditorDataAsWritable();
        String compressed;
        try {
            compressed= compress(content);
        } catch (IOException e) {throw new RuntimeException(e);}

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(compressed);
        } catch (IOException ignored) {}
        editor.mainWindow.setTitle("WireWorks V1.0 - "+editor.currentlyEditingFile.getName());
    }
}