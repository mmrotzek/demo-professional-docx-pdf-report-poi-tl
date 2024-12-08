package rocks.m2x.demo.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import rocks.m2x.demo.service.pdf.DocxToPdfService;
import rocks.m2x.demo.service.report.RenderDocxReportService;
import rocks.m2x.demo.service.report.data.Control;
import rocks.m2x.demo.service.report.data.ControlGroup;
import rocks.m2x.demo.service.report.data.SoA;
import rocks.m2x.demo.service.exc.InvalidConfigurationException;
import rocks.m2x.demo.service.exc.PdfConversionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SoaReportService {
    final RenderDocxReportService renderService;
    final DocxToPdfService docxToPdfService;

    public Pair<SoA, ByteArrayOutputStream> renderReport() throws IOException {
        SoA demoData = getDemoData();
        return Pair.of(demoData, renderService.renderSoa(demoData));
    }

    public Pair<SoA, ByteArrayOutputStream> renderReportPdf() throws PdfConversionException, IOException, InvalidConfigurationException {
        SoA demoData = getDemoData();
        try (ByteArrayOutputStream docx = renderService.renderSoa(demoData)) {
            return Pair.of(demoData, docxToPdfService.convertDocxToPdf(docx.toByteArray()));
        }
    }

    SoA getDemoData() {
        return SoA.builder()
                .created("2021-01-01")
                .version("1.0")
                .draft(false)
                .company("Demo Company<br/><u>m2x.rocks</u><br/>Demo Street 1<br/><br/><b>123456 Demo City</b>")
                .groups(List.of(
                        ControlGroup.builder()
                                .name("Organizational controls")
                                .nr("5")
                                .controls(orgControls())
                                .build(),
                        ControlGroup.builder()
                                .name("People controls")
                                .nr("6")
                                .controls(orgPeople())
                                .build(),
                        ControlGroup.builder()
                                .name("Physical controls")
                                .nr("7")
                                .controls(orgPhys())
                                .build(),
                        ControlGroup.builder()
                                .name("Technological controls")
                                .nr("8")
                                .controls(orgTech())
                                .build()
                ))
                .build();
    }

    List<Control> orgControls() {
        return List.of(
                Control.builder().nr("1").name("Policies for Information Security")
                        .description("<ul><li>Develop and maintain an information security policy aligned with organizational objectives.</li><li>Review the policy annually or upon significant changes.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("2").name("Information Security Roles and Responsibilities")
                        .description("<ul><li>Define clear roles and responsibilities for information security within the organization.</li><li>Ensure responsibilities are communicated to all relevant parties.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("3").name("Segregation of Duties")
                        .description("<ul><li>Implement controls to ensure critical tasks are divided among individuals to reduce risks.</li><li>Review roles and privileges periodically to prevent unauthorized access.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("4").name("Management Responsibilities")
                        .description("<ul><li>Ensure top management demonstrates commitment to the information security program.</li><li>Allocate resources for maintaining and improving the ISMS.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("5").name("Contact With Authorities")
                        .description("<ul><li>Establish protocols for contacting local authorities in case of a security incident.</li><li>Maintain an updated list of contact details for regulatory bodies.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("6").name("Contact With Special Interest Groups")
                        .description("<ul><li>Participate in relevant information security communities or groups.</li><li>Exchange best practices and threat intelligence.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("7").name("Threat Intelligence")
                        .description("<ul><li>Establish processes for gathering, analyzing, and acting on threat intelligence.</li><li>Integrate findings into risk management and incident response plans.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("8").name("Information Security in Project Management")
                        .description("<ul><li>Incorporate security requirements into the project lifecycle.</li><li>Conduct security risk assessments for all major projects.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("9").name("Inventory of Information and Other Associated Assets")
                        .description("<ul><li>Create and maintain an up-to-date inventory of information assets.</li><li>Classify assets based on sensitivity and importance.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("10").name("Acceptable Use of Information and Other Associated Assets")
                        .description("<ul><li>Develop an acceptable use policy outlining proper handling of information assets.</li><li>Provide regular training on acceptable use practices.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("11").name("Return of Assets")
                        .description("<ul><li>Ensure employees return all issued assets upon termination or role change.</li><li>Verify returned assets are in good condition and properly accounted for.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("12").name("Classification of Information")
                        .description("<ul><li>Define and apply information classification schemes (e.g., public, confidential).</li><li>Ensure proper handling of classified information based on its level.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("13").name("Labelling of Information")
                        .description("<ul><li>Establish labeling guidelines for sensitive and critical information.</li><li>Ensure labels are applied consistently and reviewed regularly.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("14").name("Information Transfer")
                        .description("<ul><li>Define procedures for securely transferring sensitive information.</li><li>Ensure encryption is used during transmission.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("15").name("Access Control")
                        .description("<ul><li>Implement access control policies to limit access to authorized users only.</li><li>Conduct regular audits of access privileges.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("16").name("Identity Management")
                        .description("<ul><li>Adopt robust identity verification mechanisms (e.g., multi-factor authentication).</li><li>Ensure identities are securely stored and managed.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("17").name("Authentication Information")
                        .description("<ul><li>Securely store and manage authentication credentials (e.g., passwords).</li><li>Enforce strong password policies.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("18").name("Access Rights")
                        .description("<ul><li>Assign access rights based on the principle of least privilege.</li><li>Review access rights periodically to ensure compliance.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("19").name("Information Security in Supplier Relationships")
                        .description("<ul><li>Establish security requirements for third-party suppliers.</li><li>Monitor supplier compliance with agreed security terms.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("20").name("Addressing Information Security Within Supplier Agreements")
                        .description("<ul><li>Include information security clauses in all supplier agreements.</li><li>Perform due diligence before signing new contracts.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("21").name("Managing Information Security in the ICT Supply Chain")
                        .description("<ul><li>Implement controls to manage supply chain risks.</li><li>Conduct periodic assessments of critical suppliers.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("22").name("Monitoring, Review and Change Management of Supplier Services")
                        .description("<ul><li>Regularly monitor supplier services to ensure compliance.</li><li>Manage changes to supplier agreements systematically.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("23").name("Information Security for Use of Cloud Services")
                        .description("<ul><li>Evaluate cloud providers based on their security certifications.</li><li>Implement controls to monitor cloud service usage and risks.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("24").name("Information Security Incident Management Planning and Preparation")
                        .description("<ul><li>Develop an incident response plan for potential security breaches.</li><li>Conduct regular training and simulations for the response team.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("25").name("Assessment and Decision on Information Security Events")
                        .description("<ul><li>Establish criteria to assess the severity of security events.</li><li>Ensure decisions are documented and communicated effectively.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("26").name("Response to Information Security Incidents")
                        .description("<ul><li>Define steps to contain and mitigate security incidents.</li><li>Ensure incident response is timely and well-coordinated.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("27").name("Learning From Information Security Incidents")
                        .description("<ul><li>Analyze incidents to identify root causes and lessons learned.</li><li>Update security controls and processes accordingly.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("28").name("Collection of Evidence")
                        .description("<ul><li>Follow legal and organizational guidelines for evidence collection.</li><li>Ensure evidence integrity for potential legal proceedings.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("29").name("Information Security During Disruption")
                        .description("<ul><li>Establish plans to ensure business continuity during disruptions.</li><li>Test plans regularly to ensure effectiveness.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("30").name("ICT Readiness for Business Continuity")
                        .description("<ul><li>Ensure ICT systems can support critical operations during emergencies.</li><li>Maintain redundant systems for high availability.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("31").name("Legal, Statutory, Regulatory and Contractual Requirements")
                        .description("<ul><li>Identify and comply with all relevant legal and contractual obligations.</li><li>Maintain records of compliance for audits and reviews.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("32").name("Intellectual Property Rights")
                        .description("<ul><li>Protect intellectual property through proper agreements and licenses.</li><li>Monitor for unauthorized use of organizational IP.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("33").name("Protection of Records")
                        .description("<ul><li>Implement controls to protect records from unauthorized access and loss.</li><li>Ensure record retention policies comply with legal requirements.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("34").name("Privacy and Protection of PII")
                        .description("<ul><li>Establish procedures to protect personally identifiable information (PII).</li><li>Ensure compliance with applicable data protection regulations.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("35").name("Independent Review of Information Security")
                        .description("<ul><li>Conduct periodic independent reviews of the ISMS.</li><li>Implement recommendations from the reviews to improve security posture.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("36").name("Compliance With Policies, Rules and Standards for Information Security")
                        .description("<ul><li>Monitor compliance with internal and external security standards.</li><li>Document and address any non-compliance issues promptly.</li></ul>")
                        .applicable(true).build(),

                Control.builder().nr("37").name("Documented Operating Procedures")
                        .description("<ul><li>Maintain documented operating procedures for all critical processes.</li><li>Ensure procedures are regularly reviewed and updated.</li></ul>")
                        .applicable(true).build()

        );
    }

    List<Control> orgPeople() {
        return List.of(
                Control.builder().nr("1").name("Screening").description("<ul><li>Conduct background checks on all new hires.</li><li>Verify employment history and references.</li></ul>").applicable(true).build(),
                Control.builder().nr("2").name("Terms and Conditions of Employment").description("<ul><li>Include security responsibilities in employment contracts.</li><li>Ensure contracts comply with organizational security policies.</li></ul>").applicable(true).build(),
                Control.builder().nr("3").name("Information Security Awareness, Education and Training").description("<ul><li>Provide regular security awareness training for all employees.</li><li>Develop targeted training for specific roles (e.g., IT staff).</li></ul>").applicable(true).build(),
                Control.builder().nr("4").name("Disciplinary Process").description("<ul><li>Define a clear disciplinary process for security violations.</li><li>Ensure employees are aware of consequences for non-compliance.</li></ul>").applicable(true).build(),
                Control.builder().nr("5").name("Responsibilities After Termination or Change of Employment").description("<ul><li>Ensure access rights are revoked immediately upon termination.</li><li>Conduct exit interviews to reinforce confidentiality agreements.</li></ul>").applicable(true).build(),
                Control.builder().nr("6").name("Confidentiality or Non-Disclosure Agreements").description("<ul><li>Require employees and contractors to sign NDAs before accessing sensitive information.</li><li>Periodically review and update confidentiality agreements.</li></ul>").applicable(true).build(),
                Control.builder().nr("7").name("Remote Working").description("<ul><li>Implement security controls for remote work environments (e.g., VPNs).</li><li>Conduct regular audits of remote work policies.</li></ul>").applicable(true).build(),
                Control.builder().nr("8").name("Information Security Event Reporting").description("<ul><li>Establish a centralized system for reporting security incidents.</li><li>Train employees on recognizing and reporting security events promptly.</li></ul>").applicable(true).build()
        );
    }

    List<Control> orgPhys() {
        return List.of(
                Control.builder().nr("1").name("Physical Security Perimeters").description("Remote only company; use external service providers for infrastructure").applicable(false).build(),
                Control.builder().nr("2").name("Physical Entry").description("<ul><li>Implement access control systems for entry points.</li><li>Use biometric or badge-based authentication for secure entry.</li></ul>").applicable(true).build(),
                Control.builder().nr("3").name("Securing Offices, Rooms and Facilities").description("<ul><li>Ensure all offices and facilities have secure locks.</li><li>Install alarms for unauthorized access attempts.</li></ul>").applicable(true).build(),
                Control.builder().nr("4").name("Physical Security Monitoring").description("<ul><li>Deploy surveillance systems like CCTV cameras.</li><li>Monitor entry logs and respond to anomalies promptly.</li></ul>").applicable(true).build(),
                Control.builder().nr("5").name("Protecting Against Physical and Environmental Threats").description("<ul><li>Conduct risk assessments for environmental threats.</li><li>Implement fire suppression and flood prevention systems.</li></ul>").applicable(true).build(),
                Control.builder().nr("6").name("Working In Secure Areas").description("<ul><li>Define rules for working in secure areas (e.g., no mobile phones).</li><li>Ensure authorized access is monitored.</li></ul>").applicable(true).build(),
                Control.builder().nr("7").name("Clear Desk and Clear Screen").description("<ul><li>Enforce policies for clearing desks of sensitive documents.</li><li>Enable automatic screen locks after inactivity.</li></ul>").applicable(true).build(),
                Control.builder().nr("8").name("Equipment Siting and Protection").description("<ul><li>Position equipment in secure, low-risk areas.</li><li>Provide physical protection for critical devices.</li></ul>").applicable(true).build(),
                Control.builder().nr("9").name("Security of Assets Off-Premises").description("<ul><li>Maintain inventory of off-premises assets.</li><li>Ensure secure transport and storage of equipment.</li></ul>").applicable(true).build(),
                Control.builder().nr("10").name("Storage Media").description("<ul><li>Define protocols for securely storing media (e.g., encryption).</li><li>Ensure proper labeling and access controls.</li></ul>").applicable(true).build(),
                Control.builder().nr("11").name("Supporting Utilities").description("<ul><li>Ensure backup power supply for critical systems.</li><li>Maintain HVAC systems to protect equipment.</li></ul>").applicable(true).build(),
                Control.builder().nr("12").name("Cabling Security").description("<ul><li>Conceal cables to prevent tampering.</li><li>Secure cable paths in conduits or ducts.</li></ul>").applicable(true).build(),
                Control.builder().nr("13").name("Equipment Maintenance").description("<ul><li>Schedule regular maintenance for all critical equipment.</li><li>Ensure maintenance activities are logged and reviewed.</li></ul>").applicable(true).build(),
                Control.builder().nr("14").name("Secure Disposal or Re-Use of Equipment").description("<ul><li>Define procedures for secure equipment disposal.</li><li>Ensure data is completely wiped before reuse or recycling.</li></ul>").applicable(true).build()
        );
    }

    List<Control> orgTech() {
        return List.of(
                Control.builder().nr("1").name("User Endpoint Devices").description("<ul><li>Implement security controls for all user devices.</li><li>Ensure regular updates and patches are applied.</li></ul>").applicable(true).build(),
                Control.builder().nr("2").name("Privileged Access Rights").description("<ul><li>Restrict and monitor the use of privileged accounts.</li><li>Implement multi-factor authentication for access.</li></ul>").applicable(true).build(),
                Control.builder().nr("3").name("Information Access Restriction").description("<ul><li>Use role-based access control (RBAC) to restrict access.</li><li>Audit access logs regularly to detect anomalies.</li></ul>").applicable(true).build(),
                Control.builder().nr("4").name("Access to Source Code").description("<ul><li>Implement version control systems with restricted access.</li><li>Regularly review code repositories for unauthorized changes.</li></ul>").applicable(true).build(),
                Control.builder().nr("5").name("Secure Authentication").description("<ul><li>Use strong authentication mechanisms like MFA.</li><li>Regularly review and update authentication protocols.</li></ul>").applicable(true).build(),
                Control.builder().nr("6").name("Capacity Management").description("<ul><li>Ensure sufficient resources are available for critical systems.</li><li>Regularly monitor and plan for capacity needs.</li></ul>").applicable(true).build(),
                Control.builder().nr("7").name("Protection Against Malware").description("<ul><li>Deploy antivirus and anti-malware solutions.</li><li>Regularly update malware definitions.</li></ul>").applicable(true).build(),
                Control.builder().nr("8").name("Management of Technical Vulnerabilities").description("<ul><li>Conduct regular vulnerability assessments.</li><li>Implement timely patch management processes.</li></ul>").applicable(true).build(),
                Control.builder().nr("9").name("Configuration Management").description("<ul><li>Maintain an inventory of system configurations.</li><li>Ensure changes are tracked and approved.</li></ul>").applicable(true).build(),
                Control.builder().nr("10").name("Information Deletion").description("<ul><li>Define processes for secure data deletion.</li><li>Ensure compliance with data retention policies.</li></ul>").applicable(true).build(),
                Control.builder().nr("6").name("Capacity Management").description("<ul><li>Ensure sufficient resources are available for critical systems.</li><li>Regularly monitor and plan for capacity needs.</li></ul>").applicable(true).build(),
                Control.builder().nr("7").name("Protection Against Malware").description("<ul><li>Deploy antivirus and anti-malware solutions.</li><li>Regularly update malware definitions.</li></ul>").applicable(true).build(),
                Control.builder().nr("8").name("Management of Technical Vulnerabilities").description("<ul><li>Conduct regular vulnerability assessments.</li><li>Implement timely patch management processes.</li></ul>").applicable(true).build(),
                Control.builder().nr("9").name("Configuration Management").description("<ul><li>Maintain an inventory of system configurations.</li><li>Ensure changes are tracked and approved.</li></ul>").applicable(true).build(),
                Control.builder().nr("10").name("Information Deletion").description("<ul><li>Define processes for secure data deletion.</li><li>Ensure compliance with data retention policies.</li></ul>").applicable(true).build(),
                Control.builder().nr("16").name("Monitoring Activities").description("<ul><li>Implement continuous monitoring for security threats.</li><li>Ensure monitoring covers all critical systems and networks.</li></ul>").applicable(true).build(),
                Control.builder().nr("17").name("Clock Synchronization").description("<ul><li>Ensure time synchronization across all systems.</li><li>Use secure NTP servers for synchronization.</li></ul>").applicable(true).build(),
                Control.builder().nr("18").name("Use of Privileged Utility Programs").description("<ul><li>Restrict access to utility programs with elevated privileges.</li><li>Regularly audit the use of such programs.</li></ul>").applicable(true).build(),
                Control.builder().nr("19").name("Installation of Software on Operational Systems").description("<ul><li>Implement strict controls for software installation.</li><li>Use only approved and tested software.</li></ul>").applicable(true).build(),
                Control.builder().nr("20").name("Networks Security").description("<ul><li>Deploy firewalls and intrusion detection systems (IDS).</li><li>Encrypt sensitive data transmitted over networks.</li></ul>").applicable(true).build(),
                Control.builder().nr("21").name("Security of Network Services").description("Using managed services of external service providers only").applicable(false).build(),
                Control.builder().nr("22").name("Segregation of Networks").description("<ul><li>Isolate sensitive network segments from public access.</li><li>Use VLANs or subnets for logical segregation.</li></ul>").applicable(true).build(),
                Control.builder().nr("23").name("Web Filtering").description("<ul><li>Implement web filtering solutions to block malicious websites.</li><li>Define policies to restrict access to non-work-related sites.</li></ul>").applicable(true).build(),
                Control.builder().nr("24").name("Use of Cryptography").description("<ul><li>Encrypt sensitive data in transit and at rest.</li><li>Implement secure key management practices.</li></ul>").applicable(true).build(),
                Control.builder().nr("25").name("Secure Development Life Cycle").description("<ul><li>Integrate security measures at every stage of development.</li><li>Conduct regular security code reviews and testing.</li></ul>").applicable(true).build(),
                Control.builder().nr("26").name("Application Security Requirements").description("<ul><li>Define clear security requirements for applications.</li><li>Ensure security is built into the design phase.</li></ul>").applicable(true).build(),
                Control.builder().nr("27").name("Secure System Architecture and Engineering Principles").description("<ul><li>Follow best practices for secure system architecture.</li><li>Document and review security measures regularly.</li></ul>").applicable(true).build(),
                Control.builder().nr("28").name("Secure Coding").description("<ul><li>Train developers on secure coding practices.</li><li>Use tools to identify vulnerabilities in code.</li></ul>").applicable(true).build(),
                Control.builder().nr("29").name("Security Testing in Development and Acceptance").description("<ul><li>Conduct regular security tests during development.</li><li>Include penetration testing in acceptance criteria.</li></ul>").applicable(true).build(),
                Control.builder().nr("30").name("Outsourced Development").description("<ul><li>Ensure vendors comply with security standards.</li><li>Include security requirements in contracts.</li></ul>").applicable(true).build(),
                Control.builder().nr("31").name("Separation of Development, Test and Production Environments").description("<ul><li>Physically or logically separate development, test, and production systems.</li><li>Restrict access to production systems.</li></ul>").applicable(true).build(),
                Control.builder().nr("32").name("Change Management").description("<ul><li>Document all changes and obtain approval.</li><li>Assess the security impact of each change.</li></ul>").applicable(true).build(),
                Control.builder().nr("33").name("Test Information").description("<ul><li>Ensure test data is anonymized or obfuscated.</li><li>Restrict access to test environments.</li></ul>").applicable(true).build(),
                Control.builder().nr("34").name("Protection of Information Systems During Audit Testing").description("<ul><li>Ensure audit tests do not impact production systems.</li><li>Follow guidelines to maintain data confidentiality during audits.</li></ul>").applicable(true).build()
        );
    }

}
