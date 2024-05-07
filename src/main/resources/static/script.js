window.onload = function() {
    const currentPath = window.location.pathname;

    if (currentPath.includes("ergebniss.html")) {
        loadResultPage();
    }
};

// Funktion, die beim Laden der Seite "ergebniss.html" ausgeführt wird
function loadResultPage() {
    // Hole das Bild aus dem SessionStorage
    const previewImage = sessionStorage.getItem('previewImage');

    // Zeige das Bild an, falls vorhanden
    const previewElement = document.getElementById('preview');
    if (previewImage) {
        previewElement.src = previewImage;
    } else {
        previewElement.alt = "Kein Bild gefunden.";
    }

    // Hole die gespeicherte Tabelle aus dem SessionStorage
    const tableHTML = sessionStorage.getItem('analyseTabelle');

    // Füge die gespeicherte Tabelle in den Container ein
    const resultTable = document.getElementById('resultTable');
    if (tableHTML) {
        resultTable.innerHTML = tableHTML;
    } else {
        resultTable.innerHTML = '<p>Keine Tabelle gefunden.</p>';
    }
}


function triggerAndUpload() {
    // Holen Sie sich das hochgeladene Bild
    const fileInput = document.getElementById('image');
    const files = fileInput.files;

    // Überprüfen, ob eine Datei ausgewählt ist
    if (files.length === 0) {
        alert('Bitte wählen Sie eine Datei aus.');
    } else {
        // Wenn eine Datei vorhanden ist, wird sie für den Upload an `checkFiles` übergeben
        checkFiles(files).then(() => {
            // Weiterleitung zur neuen Seite nach Abschluss der Analyse
            window.location.href = '/ergebniss.html';
        });
    }
}

function checkFiles(files) {
    if (files.length != 1) {
        alert("Bitte genau eine Datei hochladen.");
        return;
    }

    const file = files[0];
    const fileSize = file.size / 1024 / 1024; // in MiB
    if (fileSize > 10) {
        alert("Datei zu groß (max. 10 MB).");
        return;
    }

    // Lies das Bild als Base64-String und speichere es im sessionStorage
    const reader = new FileReader();
    reader.onloadend = function () {
        sessionStorage.setItem('previewImage', reader.result);
    };
    reader.readAsDataURL(file); // Base64-Kodierung

    // Bereite die Bilddaten für den Upload vor
    const formData = new FormData();
    formData.append("image", file);

    // Analysiere das Bild über einen API-Aufruf
    return fetch('/analyze', {
        method: 'POST',
        headers: {},
        body: formData
    }).then(response => response.json())
    .then(data => {
        // Generiere das HTML der Analyse-Ergebnisse
        let tableHTML = `
            <table class="table">
                <thead>
                    <tr>
                        <th>Klasse</th>
                        <th>Wahrscheinlichkeit</th>
                    </tr>
                </thead>
                <tbody>
        `;

        let count = 0;

        data.forEach(item => {
            if (item.probability > 0.1) {
                tableHTML += `
                    <tr>
                        <td>${item.className}</td>
                        <td>${(item.probability * 100).toFixed(2)}%</td>
                    </tr>
                `;
                count++;
            }
        });

        tableHTML += `
                </tbody>
            </table>
        `;

        if (count === 0) {
            tableHTML = `<p><strong>Keine klare Übereinstimmung gefunden. Versuche ein anderes Bild.</strong></p>`;
        }

        // Speichere die Ergebnisse im sessionStorage
        sessionStorage.setItem('analyseTabelle', tableHTML);
    })
    .catch(error => {
        console.log(error);
        alert("Fehler bei der Analyse der Datei.");
    });
}