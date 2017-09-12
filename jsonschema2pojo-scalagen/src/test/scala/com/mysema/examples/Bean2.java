package com.mysema.examples;

public class Bean2 {

    public static void main(String[] args) {
        Bean2 bean = new Bean2();
        bean.setFirstName("John");
        System.out.println(bean.getFirstName());
    }
    
    private int age;
    
    private String firstName;

    private String lastName;
    
    private String userName;
    
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
    
    
}
