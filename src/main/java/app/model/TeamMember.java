package app.model;

public class TeamMember {
    private String studentId;
    private String firstName;
    private String lastName;

    public TeamMember() {}

    public TeamMember(String studentId, String firstName, String lastName) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getStudentId() { 
        return studentId; 
    }
    
    public void setStudentId(String studentId) { 
        this.studentId = studentId; 
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

    @Override
    public String toString() {
        return "TeamMember{studentId='" + studentId + "', firstName='" + firstName + "', lastName='" + lastName + "'}";
    }
}
