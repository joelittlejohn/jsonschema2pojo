package com.mysema.examples;

public class BeanWithUnderscores {

    private int _age;
    
    private String _firstName;

    private String _lastName;
    
    private String _userName;
    
    public int getAge() {
        return _age;
    }

    public void setAge(int age) {
        this._age = age;
    }

    public String getFirstName() {
        return _firstName;
    }

    public void setFirstName(String firstName) {
        this._firstName = firstName;
    }

    public String getLastName() {
        return _lastName;
    }

    public void setLastName(String lastName) {
        this._lastName = lastName;
    }

    public String getUserName() {
        return _userName;
    }

    public void setUserName(String userName) {
        this._userName = userName;
    }
    
    public String toString() {
        return _firstName + " " + this._lastName;
    }
    
}
