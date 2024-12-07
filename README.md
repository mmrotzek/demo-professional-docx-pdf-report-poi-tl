# Demo: Professional template report with poi-tl

TODO:
- template 1 does not work with pdf conversion -> das template ist scheiße
- 
- template 2  ahs markup error - corrupt 
  - NOTE/IMPORTANT: 
    - HTML Handled Tags und Conditions needs linebreak / paragraph afterwads?
    - Tabellen: dafür sorgend das etews in der zelle ist - bei bedingungen linebreaks oder geht auch leerzeile?
    - LibreOffice:
      - schwierig Titelpages mit Bildern / Positionierung
      - exotische Schriften 
      - Workaround OneDrive Conversion nutzen

This demo shows how to generate a professional. word template based, report with poi-tl.
In this use case generates a **Statement of Applicability** report how it is required by ISO 27001 (Information security, cybersecurity and privacy protection).

tbd. ref auf Blogpost

To show the capabilities of poi-tl, the report can be generated based on two different templates:

1. in portrait orientation as text report, based on an existing word template (from microsoft).
2. in landscape orientation as a compact table.

Here are screenshots of the generated reports:

tbd

## Try yourself

This application is a Spring Boot application and uses Docker.

Thanks to [moalhaddar/docx-to-pdf](https://github.com/moalhaddar/docx-to-pdf),
the docx to pdf conversion can be used very easily via REST-API.

[poi-tl](https://github.com/Sayi/poi-tl) makes it very easy to create Word documents, whereby templates are simply filled with data.

### Run

Start with the following commands:

```shell
./mvnw spring-boot:run
```
or run the [DemoApplication](src/main/java/rocks/m2x/demo/DemoApplication.java) class in IDEA (you will need lombok enabled).

### Usage

- docx: [http://localhost:8080/api/soa/report](http://localhost:8080/api/soa/report)
- pdf: [http://localhost:8080/api/soa/report?format=pdf](http://localhost:8080/api/soa/report?format=pdf)

### Configuration

The configuration is done in the `application.propertie` file.

For a full list of available options see 
[ApplicationConfigurationProperties](src/main/java/rocks/m2x/demo/config/ApplicationConfigurationProperties.java).

E.g. to change the base template, set the `m2x.demo.export.template` property to the desired template file name.

```properties
# portrait as text blocks
# m2x.demo.export.template=classpath:/templates/template.docx
# landscape as table
m2x.demo.export.template=classpath:/templates/template_table.docx
```

Demo templates are located in the `src/main/resources/templates` folder.
