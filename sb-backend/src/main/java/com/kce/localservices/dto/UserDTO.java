package com.kce.localservices.dto;

public class UserDTO {
    private Integer id;
    private String name;
    private String email;
    private String role;

    public UserDTO() {
    }

    public UserDTO(com.kce.localservices.entity.User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

    public static UserDTO fromUser(com.kce.localservices.entity.User user) {
        return new UserDTO(user);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}