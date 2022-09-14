# Android-Case-Selector
Although this app was originally designed for selecting dakimakura cases, it could probably be used for many other things.

This is for all the weebs out there that have a collection of dakimakura cases that they want to rotate through regularly. After the app is setup, it will select a random case from all the images stored in the `images` directory.

### Here is a picture of the interface:
![Screenshot_20220831-114703_Case Selector](https://user-images.githubusercontent.com/105989209/187745608-f20dd339-b683-4d2c-bf5e-e0e02a9af407.jpg)

You can specify a "Safe For Work" limit that the app will not exceed.
You can also apply the limit to both sides of the case or just one by checking the "Both Sides Limitted" checkbox.
Finally, the app will avoid repeating characters when the "Avoid Repeat Characters" checkbox is ticked.
These options are automatically saved when changed and loaded when the app is opened.
The app will not repeat cases until all options are exhausted first.

If the app chooses a case (when you press "Next Case") that you want to temporarily skip, Tapping "Reselect" will pick a new case without removing the last one from the available options.
## Setup
Put all the images of cases in the `Android/data/nbradham.caseselector/files/images` directory.

All files must have the `<SFW1><SFW2><Character Name 1>[-<Character Name 2>].jpg` naming scheme.
- SFW1: A single letter representing the "safe for work" rating of the first side of the case.
- SFW2: A single letter representing the "safe for work" rating of the second side of the case.
- Character Name 1: The name of the character on the first side.
- Character Name 2: The name of the character on the second side. If not present, the program assumes the same character as on the first side.

The SFW letters can be 's' for Safe, 'e' for Erotic, or 'x' for eXplicit.
The Character Names can have any symbols that are valid in file names EXCEPT '-'. '-' is used to split the characters on the sides of the case.
