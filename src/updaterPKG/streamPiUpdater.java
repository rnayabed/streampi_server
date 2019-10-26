package updaterPKG;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class streamPiUpdater {

    public String[] makeNums(String numIn){
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
        return new String[] {firstNum, secondNum, thirdNum};
    }

    /*
    compares numbers for a version to see which is higher
     */
    public String numCompare(String softNum, String updateNum){
        int numSoft = Integer.parseInt(softNum);
        int numUpdate = Integer.parseInt((updateNum));
        if (numUpdate > numSoft) {
            return "Update";
        }
        else if (numUpdate < numSoft){
            return "Current";
        }
        else {
            return "same";
        }
    }

    /*
    This actually does a check on the whole version
     */
    public void versionCompare(String softVersion, String updateVersion){
        //STEP 1: turn each version number into an array for comparision
        String[] softArr = makeNums(softVersion);
        String[] updateArr = makeNums(updateVersion);

        //STEP 2: compare digit numbers
        String firstNum = numCompare(softArr[0], updateArr[0]);
        String secondNum = numCompare(softArr[1], updateArr[1]);
        String thirdNum = numCompare(softArr[2], updateArr[2]);

        //STEP 3: determine if update is necessary
        if (firstNum.equals("Update")){
            System.out.println("FIRST DIGIT IS BIGGER, AUTOMATIC UPDATE");
            //updateLabel.setVisible(true);
        } else if (firstNum.equals("same")){
            System.out.println("FIRST DIGIT MATCH TRYING SECOND DIGIT");
            if (secondNum.equals("Update")){
                System.out.println("SECOND NUMBER IS BIGGER, UPDATING");
                //updateLabel.setVisible(true);
            } else if (secondNum.equals("same")){
                System.out.println("SECOND DIGIT MATCH TRYING THIRD DIGIT");
                if (thirdNum.equals("Update")){
                    System.out.println("THIRD DIGIT IS BIGGER, UPDATING");
                    //updateLabel.setVisible(true);
                } else if (thirdNum.equals("same")){
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
