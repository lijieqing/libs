package com.hualee.xmlparse.xmlbean;

import com.hualee.xmlparse.annotation.XmlAttribute;
import com.hualee.xmlparse.annotation.XmlBean;

/**
 * 作者
 *
 * @author lijie
 * @create 2018-02-22 20:31
 **/
@XmlBean(name = "Writer")
public class Author {
    @XmlAttribute
    private String name;

    public Author() {
    }

    public Author(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Author{" +
                "name='" + name + '\'' +
                '}';
    }
}
