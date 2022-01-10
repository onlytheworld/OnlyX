package com.OnlyX.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by Hiroshi on 2016/8/11.
 */
@Entity
public class Source {

    @Id
    private Long id;
    @NotNull
    private String title;
    @Unique
    private int type;
    @NotNull
    private boolean enable;
//    @Property
//    private String username;
//    @Property
//    private String passwd;

    @Generated(hash = 615387317)
    public Source() {
    }

    @Generated(hash = 1339691905)
    public Source(Long id, @NotNull String title, int type, boolean enable) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.enable = enable;
    }

    

    



//    public Source(Long id, @NotNull String title, int type, boolean enable) {
//        this.id = id;
//        this.title = title;
//        this.type = type;
//        this.enable = enable;
//        this.username = null;
//        this.passwd = null;
//    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Source && ((Source) o).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id == null ? super.hashCode() : id.hashCode();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

//    public String getUsername() {
//        return this.username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getPasswd() {
//        return this.passwd;
//    }
//
//    public void setPasswd(String passwd) {
//        this.passwd = passwd;
//    }

//    public void rememberLogin(String username, String passwd) {
//        this.username = username;
//        this.passwd = passwd;
//    }
//
//    public void clearRemember() {
//        username = null;
//        passwd = null;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public String getPasswd() {
//        return passwd;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public void setPasswd(String passwd) {
//        this.passwd = passwd;
//    }

}
