function calculate() {

    const num1Input = document.getElementById("num1");
    const num2Input = document.getElementById("num2");
    const operator = document.getElementById("operator").value;
    const resultElement = document.getElementById("result");

    const num1 = num1Input.value.trim();
    const num2 = num2Input.value.trim();

    // Check empty inputs
    if (num1 === "" || num2 === "") {
        resultElement.innerText = "Please enter both numbers";
        return;
    }

    // Check valid numbers
    if (!Number.isFinite(Number(num1)) ||
        !Number.isFinite(Number(num2))) {

        resultElement.innerText = "Please enter valid numbers";
        return;
    }

    // Check division by zero
    if (operator === "/" && Number(num2) === 0) {
        resultElement.innerText = "Cannot divide by zero";
        return;
    }

    const url =
        `/calculate?num1=${encodeURIComponent(num1)}` +
        `&num2=${encodeURIComponent(num2)}` +
        `&operator=${encodeURIComponent(operator)}`;

    resultElement.innerText = "Calculating...";

    fetch(url)
        .then(response => {

            if (!response.ok) {
                throw new Error("Server error");
            }

            return response.text();
        })
        .then(result => {

            resultElement.innerText = "Result: " + result;

        })
        .catch(error => {

            console.error(error);

            resultElement.innerText =
                "Error connecting to Java server";

        });
}