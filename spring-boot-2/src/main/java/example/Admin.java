package example;

import java.util.HashSet;
import java.util.Set;

public class Admin {
    Set<String> adminSet;

    public Admin() {
        this.adminSet = new HashSet<>();
        //Add admin here
        adminSet.add("U01LE0XBXL4");//Robert Yao
        adminSet.add("U03MLAR2S9J");//Yuxiang Liu
        adminSet.add("U03F2AK4DEX");//Edmund Amankwah Jr
    }
    public boolean checkAdmin(String id){
        if(adminSet.contains(id)){
            return true;
        }
        return false;
    }
}
