package com.sap.cloud.sdk.demo.recap23.remote.utility;

import com.google.gson.JsonObject;

public class Helper {

    public static  JsonObject getUserNav(String userName){
        JsonObject userNav = new JsonObject();
        JsonObject nestedUserNav = new JsonObject();
        nestedUserNav.addProperty("uri", "User('"+userName+"')");
        nestedUserNav.addProperty("type","SFOData.User");
        userNav.add("__metadata", nestedUserNav);
        return userNav;
    }

}