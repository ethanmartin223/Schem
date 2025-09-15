* Editor space can be moved while manipulating the EditorDragArea.
Steps to reproduce:
    - Lclick then rclick immediately and drag mouse

* Redo doesnt work
  - Its just fucking broken there are no steps to reproduce, just click the button and you will see its fucked.

* Moving a Wire Node doesn't tigger history callbacks (Not undo-able)

* Components can only be moved from top half. Somewhere in the code there is a variable that only allows drags
    based on height or somthing because the elements that are same width and height work perfectly