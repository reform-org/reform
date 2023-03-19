import { PDFButton, PDFCheckBox, PDFDocument, PDFDropdown, PDFRadioGroup, PDFTextField } from "pdf-lib";

// downloads a file through the clients computer
export const download = (name, byte) => {
	var blob = new Blob([byte], { type: "application/pdf" });

	const elem = document.createElement("a");
	elem.setAttribute("href", window.URL.createObjectURL(blob));
	elem.setAttribute("download", name);
	elem.style.display = "none";
	document.body.appendChild(elem);
	elem.click();
	document.body.removeChild(elem);
};

const fill = async (uri, fields) => {
	const arrayBuffer = await fetch(uri).then((res) => res.arrayBuffer());
	const pdf = await PDFDocument.load(arrayBuffer);
	const form = pdf.getForm();

	for (let field of fields) {
		if (field.fieldType === "text") {
			try {
				form.getTextField(field.key).setText(field.value);
				form.getTextField(field.key).setFontSize(9);
			} catch (e) { }
		}

		if (field.fieldType === "checkbox" && field.value === true) {
			try {
				form.getCheckBox(field.key).check();
			} catch (e) { }
		}
	}

	return await pdf.save();
};

const getPDFFieldType = (field) => {
	let type = "";
	switch (field.constructor) {
		case PDFTextField: type = "PDFTextField"; break;
		case PDFCheckBox: type = "PDFCheckbox"; break;
		case PDFButton: type = "PDFButton"; break;
		case PDFDropdown: type = "PDFDropdown"; break;
		case PDFRadioGroup: type = "PDFRadioGroup"; break;
		default: type = "unknown"; break;
	}

	return type;
};

const getPDFFieldValue = (form, field) => {
	let value = "";
	switch (field.constructor) {
		case PDFTextField: value = form.getTextField(field.getName()).getText(); break;
		case PDFCheckBox: value = form.getCheckBox(field.getName()).isChecked() ? "checked" : ""; break;
		default: value = ""; break;
	}

	return value;
};

export const getPDFFields = async (buffer) => {
	const pdf = await PDFDocument.load(buffer);
	const form = pdf.getForm();
	const fields = form.getFields();
	const fieldDescription = [];

	fields.forEach(field => {
		const type = getPDFFieldType(field);
		const readonly = field.isReadOnly();
		const required = field.isRequired();
		const name = field.getName();
		const value = getPDFFieldValue(form, field);

		let description = `${type}: ${name}`;
		if (value !== "" && value !== undefined) description += ` [${value}]`;
		if (required) description += " (required)";
		if (readonly) description += " (readonly)";
		fieldDescription.push(description);
	});

	return fieldDescription;
};

export const fillPDF = async (uri, fields) => {
	return await fill(uri, fields);
};

export const fillAndDownloadPDF = async (uri, filename, fields) => {
	const outputBuffer = await fill(uri, fields);
	download(filename, outputBuffer);

	return outputBuffer;
};
