document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("reportForm");
  const textarea = document.getElementById("reportDataJson");
  const htmlFields = document.getElementById("htmlFields");
  const errorBox = document.getElementById("json-error");
  const formatButton = document.getElementById("formatJsonButton");
  const loadSampleButton = document.getElementById("loadSampleJsonButton");
  const loadSampleInvoiceJsonButton = document.getElementById("loadSampleInvoiceJsonButton");
  const templateUploadFile = document.getElementById("templateUploadFile");
  const uploadTemplateButton = document.getElementById("uploadTemplateButton");
  const templateUploadStatus = document.getElementById("templateUploadStatus");
  const templateSelect = document.getElementById("templateId");

  if (!form || !textarea) {
    return;
  }

  function showError(message) {
    if (!errorBox) {
      return;
    }
    errorBox.textContent = message;
    errorBox.classList.add("visible");
  }

  function clearError() {
    if (!errorBox) {
      return;
    }
    errorBox.textContent = "";
    errorBox.classList.remove("visible");
  }

  function tryFormatJson() {
    const raw = textarea.value.trim();
    if (!raw) {
      return;
    }
    try {
      const parsed = JSON.parse(raw);
      textarea.value = JSON.stringify(parsed, null, 2);
      clearError();
    } catch (e) {
      showError("Invalid JSON: " + e.message);
    }
  }

  form.addEventListener("submit", (event) => {
    clearError();

    const raw = textarea.value.trim();
    if (!raw) {
      event.preventDefault();
      showError("Please provide JSON data before generating a report.");
      return;
    }

    try {
      const parsed = JSON.parse(raw);
      textarea.value = JSON.stringify(parsed, null, 2);
    } catch (e) {
      event.preventDefault();
      showError("Invalid JSON: " + e.message);
    }
  });

  if (formatButton) {
    formatButton.addEventListener("click", (event) => {
      event.preventDefault();
      tryFormatJson();
    });
  }

  if (loadSampleButton) {
    loadSampleButton.addEventListener("click", async (event) => {
      event.preventDefault();
      try {
        const r = await fetch("/ui/reports/sample-json");
        if (!r.ok) throw new Error(r.statusText);
        const json = await r.text();
        textarea.value = json;
        htmlFields.value = 'company, description';
        clearError();
      } catch (e) {
        showError("Could not load sample SoA data: " + e.message);
      }
    });
  }
  if (loadSampleInvoiceJsonButton) {
    loadSampleInvoiceJsonButton.addEventListener("click", async (event) => {
      event.preventDefault();
      try {
        const r = await fetch("/ui/reports/sample-json-invoice");
        if (!r.ok) throw new Error(r.statusText);
        const json = await r.text();
        textarea.value = json;
        htmlFields.value = 'notes';
        clearError();
      } catch (e) {
        showError("Could not load sample Invoice data: " + e.message);
      }
    });
  }

  function showTemplateUploadStatus(message, isError = false) {
    if (!templateUploadStatus) return;
    templateUploadStatus.style.display = "block";
    templateUploadStatus.className = isError 
      ? "alert alert-danger mt-2" 
      : "alert alert-success mt-2";
    templateUploadStatus.textContent = message;
    if (!isError) {
      setTimeout(() => {
        templateUploadStatus.style.display = "none";
      }, 5000);
    }
  }

  function refreshTemplateDropdown() {
    if (!templateSelect) return;
    
    fetch("/ui/reports/templates")
      .then(r => {
        if (!r.ok) throw new Error(r.statusText);
        return r.json();
      })
      .then(data => {
        // Find the "Uploaded templates" optgroup or create it
        let uploadedGroup = templateSelect.querySelector('optgroup[label="Uploaded templates"]');
        if (!uploadedGroup) {
          // Remove existing uploaded templates options first
          const existingUploaded = templateSelect.querySelectorAll('optgroup[label="Uploaded templates"] option');
          existingUploaded.forEach(opt => opt.remove());
          
          // Create new optgroup
          uploadedGroup = document.createElement("optgroup");
          uploadedGroup.label = "Uploaded templates";
          templateSelect.appendChild(uploadedGroup);
        } else {
          // Clear existing options in the group
          uploadedGroup.innerHTML = "";
        }

        // Add uploaded templates
        const uploadedTemplates = data.uploadedTemplates || {};
        if (Object.keys(uploadedTemplates).length > 0) {
          Object.entries(uploadedTemplates).forEach(([id, metadata]) => {
            const option = document.createElement("option");
            option.value = id;
            option.textContent = `${metadata.name || id} (${id})`;
            uploadedGroup.appendChild(option);
          });
        } else {
          // Remove the group if no templates
          uploadedGroup.remove();
        }
      })
      .catch(e => {
        console.error("Failed to refresh template list:", e);
      });
  }

  if (uploadTemplateButton && templateUploadFile) {
    uploadTemplateButton.addEventListener("click", async (event) => {
      event.preventDefault();
      
      const file = templateUploadFile.files[0];
      if (!file) {
        showTemplateUploadStatus("Please select a template file to upload.", true);
        return;
      }

      if (!file.name.toLowerCase().endsWith(".docx")) {
        showTemplateUploadStatus("Only .docx files are supported.", true);
        return;
      }

      // Disable button during upload
      uploadTemplateButton.disabled = true;
      uploadTemplateButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Uploading...';

      try {
        const formData = new FormData();
        formData.append("templateFile", file);
        
        // Extract name from filename (remove .docx extension)
        const templateName = file.name.replace(/\.docx$/i, "");
        formData.append("templateName", templateName);

        const r = await fetch("/ui/reports/upload-template", {
          method: "POST",
          body: formData
        });

        const contentType = r.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
          const result = await r.json();
          if (result.success) {
            showTemplateUploadStatus(`Template "${result.templateName}" uploaded successfully! It will appear in the template dropdown.`);
            templateUploadFile.value = ""; // Clear file input
            refreshTemplateDropdown();
            // Select the newly uploaded template
            if (templateSelect) {
              templateSelect.value = result.templateId;
            }
          } else {
            showTemplateUploadStatus(result.message || "Upload failed.", true);
          }
        } else {
          const errorText = await r.text();
          showTemplateUploadStatus(errorText || "Upload failed.", true);
        }
      } catch (e) {
        showTemplateUploadStatus("Error uploading template: " + e.message, true);
      } finally {
        uploadTemplateButton.disabled = false;
        uploadTemplateButton.innerHTML = '<i class="bi bi-upload"></i> Upload Template';
      }
    });
  }
});

