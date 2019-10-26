package updaterPKG;

public class softwareTag {

    private String versionNum;
    private String softName;

    //Constructor
    public softwareTag(String vNumIn, String softNameIn){
        this.versionNum = vNumIn;
        this.softName = softNameIn;
    }

    //changes the version number
    public void updateVNum(String vNumIn){
        this.versionNum = vNumIn;
    }

    //getters
    //returns Version Number
    public String getVersionNum(){
        return versionNum;
    }
    //returns software name
    public String getSoftName() {
        return softName;
    }
}
