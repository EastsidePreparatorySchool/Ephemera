/*
 SpaceCritters online - GM, QB, PM
 */




// utilities
//

function print(message) {
    message = textarea.value + message;
    textarea.value = message.substr(-20000);
    textarea.scrollTop = textarea.scrollHeight;
}


function println(message) {
    print(message + "\n");
}

function dprintln(message) {
    if (debug) {
        console.log(message);
    }
}


function assert(condition, action) {
    if (debug) {
        if (!condition()) {
            dprintln("assert failed, see console for details: " + condition);
            if (action !== undefined) {
                action();
            }
            throw new Error("assert failed");
        } else {
//println("survived assert " + lambda);
        }
    }
}


function listAliens() {
    let a;
    let count = 0;
    for (a in aliens) {
        let alien = aliens[a];
        println(" id:" + alien.id + ", (" + (-alien.mesh.position.z) + "," + (-alien.mesh.position.x) + "), h:" + alien.mesh.position.y);
        if (count++ > 100) {
            break;
        }
    }
}
