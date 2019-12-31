package updaterPKG;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.stream.Stream;

public class streamPiUpdater {

    public String[] makeNums(String numIn){

        /*
        //first num
        int index = numIn.indexOf(".");
        String firstNum = numIn.substring(0, index);
        String rest = numIn.substring(index + 1);
        //second num
        int indexTwo = rest.indexOf(".");
        String secondNum = rest.substring(0, indexTwo);
        //third number
        String thirdNum = rest.substring(indexTwo + 1);
        //make magic happen
        //return new String[] {firstNum, secondNum, thirdNum};
         */

        return numIn.split("\\.");
    }

    public streamPiUpdater(String softVersion, boolean isServerUpdate)
    {
        isUpdateAvailable = checkForUpdates(softVersion,isServerUpdate);
    }

    /*
    compares numbers for a version to see which is higher
     */
    public int numCompare(String softNum, String updateNum){
        int numSoft = Integer.parseInt(softNum);
        int numUpdate = Integer.parseInt((updateNum));

        System.out.println(numSoft+","+numUpdate);
        if (numUpdate > numSoft) {
            return 1;
        }
        else if (numUpdate < numSoft){
            return 2;
        }
        else {
            return 3;
        }
    }

    private String newVersion;
    private String currentVersion;
    public boolean isServerUpdate;
    public boolean isUpdateAvailable;
    private String changelogRaw;
    private String downloadLink;

    public String getCurrentVersion()
    {
        return currentVersion;
    }

    public String getNewVersion()
    {
        return newVersion;
    }

    public String getDownloadLink()
    {
        return downloadLink;
    }

    public boolean isUpdateAvailable()
    {
        return isUpdateAvailable;
    }

    public String getChangelogRaw()
    {
        return changelogRaw;
    }

    /*
    This actually does a check on the whole version
     */
    private boolean checkForUpdates(String softVersion, boolean isServerUpdate){
        try
        {
            currentVersion = softVersion;
            gitRepo serverRepo;
            if(isServerUpdate)
                serverRepo = new gitRepo("https://api.github.com/repos/ladiesman6969/streampi_server/releases");
            else
                serverRepo = new gitRepo("https://api.github.com/repos/ladiesman6969/streampi_client/releases");

            newVersion = serverRepo.getRepoVer();
            changelogRaw = serverRepo.getChangelog();
            downloadLink = serverRepo.getDownloadLink();


            String[] sMap = currentVersion.split("\\.");
            String[] uMap = newVersion.split("\\.");

            if(Integer.parseInt(uMap[0]) > Integer.parseInt(sMap[0]))
                return true;
            else
            {
                if(Integer.parseInt(uMap[1]) > Integer.parseInt(sMap[1]))
                    return true;
                else
                if(Integer.parseInt(uMap[2]) > Integer.parseInt(sMap[2]))
                    return true;
            }

            return false;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }


        /*
        //STEP 1: turn each version number into an array for comparision
        String[] softArr = makeNums(softVersion);
        String[] updateArr = makeNums(updateVersion);

        //STEP 2: compare digit numbers
        int firstNum = numCompare(softArr[0], updateArr[0]);
        int secondNum = numCompare(softArr[1], updateArr[1]);
        int thirdNum = numCompare(softArr[2], updateArr[2]);

        boolean toBeReturned = false;
        //STEP 3: determine if update is necessary
        if (firstNum == 1){
            System.out.println("FIRST DIGIT IS BIGGER, AUTOMATIC UPDATE");
            toBeReturned = true;
            //updateLabel.setVisible(true);
        } else if (firstNum == 2){
            System.out.println("FIRST DIGIT MATCH TRYING SECOND DIGIT");
            if (secondNum == 1){
                System.out.println("SECOND NUMBER IS BIGGER, UPDATING");
                //updateLabel.setVisible(true);
                toBeReturned = true;
            } else if (secondNum == 2){
                System.out.println("SECOND DIGIT MATCH TRYING THIRD DIGIT");
                if (thirdNum == 1){
                    System.out.println("THIRD DIGIT IS BIGGER, UPDATING");
                    toBeReturned = true;
                    //updateLabel.setVisible(true);
                } else if (thirdNum == 2){
                    System.out.println("YOU ARE ON THE MOST UP TO DATE VERSION");
                } else {
                    System.out.println("USE CURRENT VERSION YOUR 3 IS BIGGER");
                }
            } else {
                System.out.println("USE CURRENT VERSION YOUR 2 IS BIGGER");
            }
        } else {
            System.out.println("USE CURRENT VERSION YOUR 1 IS BIGGER");
        }

        return toBeReturned;
         */
    }

    /*
    checks for updates
     */
//    public void checkForUpdates(){
//        updateLabel.setVisible(false);
//        softwareTag tag = new softwareTag(versionNum.getText(), softName.getText());
//
//        gitRepo client = new gitRepo("https://api.github.com/repos/ladiesman6969/streampi_client/releases");
//        client.repoRequest();
//
//        PauseTransition visiblePause = new PauseTransition(
//                Duration.seconds(1)
//        );
//        PauseTransition visiblePauseTwo = new PauseTransition(
//                Duration.seconds(1)
//        );
//        visiblePause.setOnFinished(
//                event -> versionCompare(tag.getVersionNum(), client.getRepoVer())
//        );
//        visiblePauseTwo.setOnFinished(
//                event -> System.out.println("UPDATES CHECKED")
//        );
//        visiblePause.play();
//        visiblePauseTwo.play();
//    }
//
}
