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
| Thomas Young | S3968936 | Sub-Task B: Homepage, Mission Statement, Personas, Feedback, Privacy Policy |
| Yukheang Kang | S4055262 | Sub-Task A: Explore Data, Trending, Insights |

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
| Homepage | Displays key statistics at a glance including top countries by vaccination coverage, economic development overview, regional performance, and disease burden data. |
| Explore Data | Allows users to filter and analyse vaccination coverage data by country, region, vaccine type, and year range with interactive charts. |
| Trending | Displays infection trends by economic status, allowing comparison of disease burden across different economic development groups. |
| Insights | Interactive world map showing global vaccination coverage rates by country with detailed country-level data tables. |
| Mission Statement | Explains the project mission, target user personas, sub-task breakdown, and team contributors. |
| Feedback | Allows users to submit feedback to help improve the platform. |
| Privacy Policy | Outlines data collection and privacy practises for the educational platform. |

---

## Personas
The dashboard is designed around three main user personas:

1. **Ms. Sarah Thomas – Private High School Teacher**
   A 33-year-old teacher from Toronto with two young children who needs easy access to vaccination schedules and reliable information about vaccination rates.
   Prefers visual data and clear language for both personal and classroom use.

2. **Dr. Henry Collins – Public Health Researcher**
   A 62-year-old semi-retired researcher from Melbourne who requires comprehensive immunisation datasets and tools to compare vaccination trends across countries.
   Values accuracy, data transparency, downloadable datasets, and filtering capabilities for research and analysis.

3. **Mrs. Priya Kapoor – Retail Store Assistant**
   A 29-year-old from Manchester who needs straightforward explanations without technical jargon and simple, trustworthy visuals.
   Prefers plain language, clear layouts, and visual information to understand vaccination and infection trends.

---

## Technologies Used
- Java (Javalin Framework)
- Thymeleaf (Template Engine)
- HTML5 / CSS3
- JavaScript (Google Charts API)
- SQLite Database
- JDBC for database connectivity
- Maven for build management
- Git / GitHub for version control

---

## How to Run
1. Clone or download the project folder.
2. Ensure you have Java 17+ and Maven installed.
3. Open the project in your IDE (e.g., IntelliJ or VS Code).
4. Build the project using Maven:
   ```bash
   mvn clean compile
   ```
5. Run the `App.java` file located in `src/main/java/app/`.
6. Open your browser and navigate to:
   ```
   http://localhost:7001/
   ```
7. Explore the site using the navigation bar or footer links.

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
- Clean, flat design with consistent styling across all pages
- Accessible navigation with clear visual hierarchy
- Consistent colour scheme with blue (#0078d7) as primary accent colour
- High contrast for readability with alternating table row colours
- Footer and navigation bar included on every page for consistency
- Interactive charts using Google Charts API for data visualisation
- Comprehensive data export functionality (CSV and PDF formats)

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
