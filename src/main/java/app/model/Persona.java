package app.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Persona {
    private int personaId;
    private String name;
    private int age;
    private String occupation;
    private String education;
    private String location;
    private String language;
    private String disability;
    private String needs;
    private String goals;
    private String skills;
    private String imageCredit;

    // --- Constructor ---
    public Persona(int personaId, String name, int age, String occupation, String education, 
                   String location, String language, String disability,
                   String needs, String goals, String skills, String imageCredit) {
        this.personaId = personaId;
        this.name = name;
        this.age = age;
        this.occupation = occupation;
        this.education = education;
        this.location = location;
        this.language = language;
        this.disability = disability;
        this.needs = needs;
        this.goals = goals;
        this.skills = skills;
        this.imageCredit = imageCredit;
    }

    // --- No-arg constructor (---
    public Persona() {

    }

    // --- Getters and Setters ---
    public int getPersonaId() { 
        return personaId; 
    }

    public void setPersonaId(int personaId) { 
        this.personaId = personaId; 
    }

    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }


    public int getAge() { 
        return age; 
    }
    
    public void setAge(int age) { 
        this.age = age; 
    }

    public String getOccupation() { 
        return occupation; 
    }
    
    public void setOccupation(String occupation) { 
        this.occupation = occupation; 
    }

    public String getEducation() { 
        return education; 
    }
    
    public void setEducation(String education) { 
        this.education = education; 
    }

    public String getLocation() { 
        return location; 
    }
    
    public void setLocation(String location) { 
        this.location = location; 
    }

    public String getLanguage() { 
        return language; 
    }
    
    public void setLanguage(String language) { 
        this.language = language; 
    }

    public String getDisability() { 
        return disability; 
    }
    
    public void setDisability(String disability) { 
        this.disability = disability; 
    }

    public String getNeeds() { 
        return needs; 
    }
    
    public void setNeeds(String needs) { 
        this.needs = needs; 
    }

    public String getGoals() { 
        return goals; 
    }
    
    public void setGoals(String goals) { 
        this.goals = goals; 
    }

    public String getSkills() { 
        return skills; 
    }
    
    public void setSkills(String skills) { 
        this.skills = skills; 
    }

    public String getImageCredit() { 
        return imageCredit; 
    }
    
    public void setImageCredit(String imageCredit) { 
        this.imageCredit = imageCredit; 
    }

    // --- Derived lists for Thymeleaf ---
    public List<String> getNeedsList() {
        return parseBulletPoints(needs);
    }

    public List<String> getGoalsList() {
        return parseBulletPoints(goals);
    }

    public List<String> getSkillsList() {
        return parseBulletPoints(skills);
    }

    // Utility method to split bullet points
    private List<String> parseBulletPoints(String text) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        return Arrays.asList(text.split("\\s*[-;]\\s*"));
    }
}
