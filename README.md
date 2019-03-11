# SpireRunInfo

SpireRunInfo is a utility made for Slay the Spire by Mega Crit Games. It reads the most recent save file from your save directory and records a variety of info about what occurred on each floor, and uploads it to a specified Google Sheet.

### Prerequisites

You will need Java 8 or higher in order to run this program.

### Installing

First, make a copy of the [template spreadsheet](https://docs.google.com/spreadsheets/d/1iymLk7cSTh5YNmj-ZiUGedZNRQ43gd8XSLBeYiVSyfw/edit?usp=sharing) so that you have a copy on your own account. While you're there, copy down the spreadsheet's ID, located in the URL:

```
https://docs.google.com/spreadsheets/d/[Spreadsheet ID]/edit#gid=0
```

Now download the latest release of SpireRunInfo, and extract it somewhere on your computer. Before running, you will need to open up the config.json file and paste the spreadsheet ID over the one that's already there. While you have the config open, double check that the "saveDirectory" property is correctly pointing to your Slay the Spire save folder.

That's it! Run SpireRunInfo.jar and you're good to go. One last thing to keep in mind is that the first time you run it, you will need to authorize the program through your Google account. Google will probably tell you that the program is unsafe (which you can bypass by clicking on "Advanced"), but there's not much I can do about that until Google verifies SpireRunInfo. If you're concerned, the source code is all right here for you to peruse.

## Author

* **Davi DeGanne** - [DaviBones](https://github.com/DaviBones)

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details
