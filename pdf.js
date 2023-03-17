import { PDFDocument } from "pdf-lib";

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
}

export const getPDFFields = async (buffer) => {
	const pdf = await PDFDocument.load(buffer);
	const form = pdf.getForm();
	const fields = form.getFields()
	const fieldDescription = []
	fields.forEach(field => {
		const type = field.constructor.name
		const required = field.isRequired()
		const name = field.getName()
		let description = `${type}: ${name}`
		if (required) description += " (required)"
		fieldDescription.push(description)
	})

	return fieldDescription
}

export const fillPDF = async (uri, fields) => {
	return await fill(uri, fields);
};

export const fillAndDownloadPDF = async (uri, filename, fields) => {
	const outputBuffer = await fill(uri, fields);
	download(filename, outputBuffer);

	return outputBuffer;
};
