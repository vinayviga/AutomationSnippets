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
        console.log(`\x1b[32m Rest Assured, your order is being prepared and delivered on time even if it had been only
             ${deliveryTime} minutes since you placed the order ( ͡ᵔ ͜ʖ ͡ᵔ ) \x1b[0m`);
        promptUser();
    } else if (number === 2) {
        console.log("\x1b[32m I see that a delivery partner has not been assigned yet, rest assured your order is running on time " +
            "\nand once the food gets cold, rotten and inedible ლ(´ڡ`ლ) a delivery partner would be assigned (^̮^) \x1b[0m");
        promptUser();
        } else if (number === 3) {

        console.log("\x1b[32m That is sad to hear, we know that you have ripped the packaging to shreds and already digested half the food ( ͡ᵔ ͜ʖ ͡ᵔ ) " +
            "\nso kindly take a picture of the damaged/ missing item along with the packaging so that we can process the smallest " +
            "\npossible portion of the amount as refund, choose your option (ᵔᴥᵔ) \x1b[0m");
        
        console.log('\x1b[34m 3.1 I have no respect for my money. Just wanted to complain \x1b[0m');
        console.log('\x1b[34m 3.2 I know you are going to parasite your way through my refund, but still, refund whatever you can \x1b[0m');
        issue3();

    }  else if(number === 4){
        console.log("\x1b[32m Kindly reach out to the delivery parter, he will not pick up your call and you have to chase him,"+
            "\n race him and catch him to get your food. Rest assured your order is running on time (^̮^) \x1b[0m"
        );
        promptUser();
    } else if (number === 5) {
        console.log("\x1b[32m Glad we were able to help and you were able to come to your senses ◕‿◕ \x1b[0m");
        rl.close(); // Close input stream and exit
        return;
    }
    // Increase delivery time by 10 for every selection except exit
    deliveryTime += 10;
     // Ask again until user exits
}

function promptUser() {
    
    console.log(`\n \x1b[31m Expected delivery time is \x1b[35m${deliveryTime}\x1b[0m \x1b[31mminutes, please select the issue: \x1b[0m`);
    console.log("\x1b[31m 1. My order is delayed \x1b[0m");
    console.log("\x1b[31m 2. Rider is not assigned to my order \x1b[0m");
    console.log("\x1b[31m 3. Items are missing/damaged from the order \x1b[0m");
    console.log("\x1b[31m 4. The delivery rider is moving in the wrong direction/ not moving \x1b[0m");
    console.log('\x1b[31m 5. I realized this is a waste of time \x1b[0m');

    rl.question("Enter the issue number: ", checkNumber);
}

function issue3()
{
    rl.question("Enter the issue number: ", function(input) {
       
    
    let number = input.trim();
    if(number === "3.1")
    {
        console.log('\x1b[34m Thats great! Do order food from this platform itself but from a different restaurant '+
            '\n so we may loot you more! Glad we were able to help ( ͡ᵔ ͜ʖ ͡ᵔ ) \x1b[0m'
        );
    } else if(number === "3.2")
    {
        console.log('\x1b[34m We are sorry to give away a portion of the item\'s cost back to you (¬_¬), your "refund"  '+
            '\nwill be processed within 3-4 business days \x1b[0m');
    } else
    {
        console.log("\x1b[34m It's either 3.1 or 3.2, make up your mind! \x1b[0m");
        issue3(); // Ask again if input is invalid
        return;
    }
    
    promptUser();
});
}

promptUser(); // Start the prompt
