package com.annie.parstagram;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Post.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("zBmty2IuZFUUhB9glcUcM87QVTmLJY28WsgQ46Jt")
                .clientKey("EEdoIIuz2lNTSV4Rzp5hvyzaWRa4p9OgfF4lZPKh")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
