package com.rafo.hall.vo;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

/**
 * Created by Administrator on 2016/11/21.
 */
public class ActiveListVO {
    private String id;
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SFSObject toObject() {
        SFSObject object = new SFSObject();
        object.putUtfString("id" , id);
        object.putUtfString("url" , url);

        return object;
    }
}
