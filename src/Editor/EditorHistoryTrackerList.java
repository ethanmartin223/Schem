package Editor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

public class EditorHistoryTrackerList extends JPanel {

    JList<String> historyList;
    JList<String> futureHistoryList;
    ArrayList<String> historyData;
    ArrayList<String> futureHistoryData;

    public EditorHistoryTrackerList() {
        setLayout(new BorderLayout());
        historyList = new JList<>();
        futureHistoryList = new JList<>();
        historyData = new ArrayList<>();
        futureHistoryData = new ArrayList<>();
        add(futureHistoryList, BorderLayout.SOUTH);
        add(historyList, BorderLayout.NORTH);
    }

    public void addEntry(String s) {
        historyData.add(s);
        historyList.setListData(historyData.toArray(new String[historyData.size()]));
        historyList.revalidate();
        revalidate();
    }

    public void removeLast() {
        historyData.remove(historyData.getLast());
        historyList.setListData(historyData.toArray(new String[historyData.size()]));
        historyList.revalidate();
        revalidate();
    }
}
