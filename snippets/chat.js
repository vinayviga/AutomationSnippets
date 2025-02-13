const readline = require('readline');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

let deliveryTime = 20;


function checkNumber(input) {
    let number = parseInt(input); 
    
    if (isNaN(number) || number < 0 || number > 5) {
        console.log("Please enter a valid issue number.");
    } else if (number === 1) {
        console.log(`Rest Assured, your order is being prepared and delivered on time even if it had been only ${deliveryTime} minutes since you placed the order ( ͡ᵔ ͜ʖ ͡ᵔ )`);
    } else if (number === 2) {
        console.log("I see that a delivery partner has not been assigned yet, rest assured your order is running on time " +
            "\nand once the food gets cold, rotten and inedible ლ(´ڡ`ლ) a delivery partner would be assigned (^̮^)");
    } else if (number === 3) {
        console.log("That is sad to hear, we know that you have ripped the packaging to shreds and already digested half the food ( ͡ᵔ ͜ʖ ͡ᵔ ) " +
            "\nso kindly take a picture of the damaged/ missing item along with the packaging so that we can process the smallest " +
            "\npossible portion of the amount as refund (ᵔᴥᵔ)");
    } else if (number === 4) {
        console.log("Glad we were able to help and you were able to come to your senses ◕‿◕");
        rl.close(); // Close input stream and exit
        return;
    }
    // Increase delivery time by 10 for every selection except 4
    deliveryTime += 10;
    promptUser(); // Ask again until user exits
}

function promptUser() {
    
    console.log(`\nExpected delivery time is ${deliveryTime} minutes, please select the issue:`);
    console.log("1. My order is delayed");
    console.log("2. Rider is not assigned to my order");
    console.log("3. Items are missing/damaged from the order");
    console.log("4. I realized this is a waste of time");

    rl.question("Enter the issue number: ", checkNumber);
}

promptUser(); // Start the prompt
