# Global Health Dashboard

### COSC2803 – Java Programming Studio 1  
Semester 2, 2025 | RMIT University  
Project Theme: Global Vaccination Initiatives  

---

## Overview
The Global Health Dashboard is a data-driven web application designed to explore and visualise worldwide vaccination and infection trends.  
It supports transparent, evidence-based insights into global health data to help users understand immunisation coverage, infection rates, and their relationships across countries, regions, and economic statuses.

This project forms part of the COSC2803 Java Programming Studio 1 social challenge:  
“Preventable Infectious Diseases – Global Vaccination Initiatives.”

---

## Authors
| Name | Student ID | Role |
|------|-------------|------|
| Thomas Young | S3968936 | Sub-Task A: Homepage, Explore Data (Vaccination), Insights |
| Yukheang Kang | S4055262 | Sub-Task B: Mission Statement, Explore Data (Economic Status), Feedback |

---

## Purpose
The Global Health Dashboard aims to:
- Present vaccination and infection data in a clear, unbiased, and accessible way.  
- Support different user groups — from parents and teachers to public health researchers — in exploring global health data.  
- Enable users to identify trends, patterns, and correlations across regions and economic phases.  
- Promote transparency and data literacy in the context of global vaccination initiatives.

---

## Key Pages
| Page | Description |
|------|--------------|
| Homepage | Provides an overview of the dashboard and key global vaccination facts. |
| Explore Data | Allows users to view and filter vaccination or infection data by region, country, year, and economic status. |
| Trending | Displays key global vaccination and infection trends over time. |
| Insights | Summarises findings, correlations, and visual summaries to support understanding. |
| Mission Statement | Explains the purpose, audience, and ethical goals of the project. |
| Feedback | Allows users to share their thoughts or request additional datasets or features. |

---

## Personas
The dashboard is designed around two main user personas:

1. **Sarah Thomas – Concerned Parent / Teacher**  
   Needs simple, reliable vaccination summaries to make informed decisions and educate others.  
   Prefers visual data and clear language.

2. **Dr. Henry Collins – Public Health Researcher**  
   Requires access to detailed, downloadable datasets for analysis and reporting.  
   Values accuracy, data transparency, and filtering capabilities.

---

## Technologies Used
- Java (Javalin Framework)  
- HTML5 / CSS3  
- SQLite Database  
- JDBC for database connectivity  
- Git / GitHub for version control  

---

## How to Run
1. Clone or download the project folder.  
2. Ensure you have Java 17+ and SQLite installed.  
3. Open the project in your IDE (e.g., IntelliJ or Eclipse).  
4. Build and run the `App.java` file.  
5. Open your browser and navigate to:  
   [[http://localhost:7001/]]
6. Explore the site using the navigation bar or footer links.

---

## Database
The provided SQLite database contains tables for:
- `Country`, `Region`, `Economy`
- `Infection_Type`, `InfectionData`
- `Antigen`, `VaccinationData`
- `Personas` and `PersonaAttributes`

Queries support filtering, aggregation, and comparison of data across time and geography.

---

## Design Considerations
- Consistent, accessible layout following Nielsen’s UI heuristics.  
- Inclusive design with high contrast, readable fonts, and accessible navigation.  
- Footer and navigation bar included on every page for consistency.  
- Supports visual exploration and textual interpretation of global data.

---

## Academic Acknowledgement
This project was developed as part of RMIT University’s COSC2803 – Java Programming Studio 1, Semester 2, 2025.  
All data and figures are for educational purposes only and may not reflect real-world WHO or CDC datasets.

---

## License
© 2025 RMIT University  
This project is submitted for academic purposes.  
Unauthorized reproduction or redistribution is prohibited.

---

## Contact
For queries or collaboration:  
Thomas Young – S3968936@student.rmit.edu.au  
Yukheang Kang – S4055262@student.rmit.edu.au

---

*"Empowering informed global health decisions through accessible, transparent data."*
