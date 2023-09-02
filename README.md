# Android-Case-Selector
Although this app was originally designed for selecting dakimakura cases, it could probably be used for many other things.

This is for all the weebs out there that have a collection of dakimakura cases that they want to rotate through regularly. After the app is setup, it will select a random case from all the images stored in the `images` directory.

### Here is a picture of the interface:
![Screenshot_20220831-114703_Case Selector](https://user-images.githubusercontent.com/105989209/187745608-f20dd339-b683-4d2c-bf5e-e0e02a9af407.jpg)

You can specify a "Safe For Work" limit that the app will not exceed.
You can also apply the limit to both sides of the case or just one by checking the "Both Sides Limited" checkbox.
Finally, the app will avoid repeating characters when the "Avoid Repeat Characters" checkbox is ticked.
These options are automatically saved when changed and loaded when the app is opened.
The app will not repeat cases until all options are exhausted first.

If the app chooses a case (when you press "Next Case") that you want to temporarily skip, Tapping "Reselect" will pick a new case without removing the last one from the available options.
## Setup
Put all the images of cases in the `Android/data/nbradham.caseselector/files/images` directory.

All files must have the `[fileDiff]<SFW1><SFW2><char0ID>[char1ID...].jpg` naming scheme.
- fileDiff: (Optional) if you have filenames that would clash, the first character of the name can be a 0-9 digit and is ignored by the app.
- SFW1: A single letter representing the "safe for work" rating of the first side of the case.
- SFW2: A single letter representing the "safe for work" rating of the second side of the case.
- char0ID: The single character, case sensitive, ID of the character on case.
- char1ID...: (Optional) The single character, case sensitive, ID(s) of the other character(s) on case.

The SFW letters can be 's' for Safe, 'e' for Erotic, or 'x' for eXplicit.
The Character IDs can be any symbols that are valid in file names.

The example above could have the filename `ss1.jpg`. Sides are safe, hence the double 's' and there is only one character present so their is only one character id.