package com.mysema.examples;

public class Bean {

    public static void main(String[] args) {
        Bean bean = new Bean();
        bean.setFirstName("John");
        System.out.println(bean.getFirstName());
    }
    
    private int age;
    
    private String firstName, lastName, userName;
    
    private boolean male;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }
        
}
