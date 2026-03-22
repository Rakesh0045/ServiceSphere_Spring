package com.kce.localservices.dto;

public class UserDTO {
    private Integer id;
    private String name;
    private String email;
    private String role;

    public UserDTO(com.kce.localservices.entity.User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}