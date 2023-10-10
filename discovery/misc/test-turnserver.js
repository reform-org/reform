import wrtc from "wrtc";

function testIceServers(iceServers) {
    return new Promise((resolve, reject) => {
        const pc = new wrtc.RTCPeerConnection({
            iceServers
        });

        const fail = (error) => {
            console.error("Test for", iceServers, "failed");
            if (error) {
                console.error(error);
            }
            reject(error);
        }

        const failTimeout = setTimeout(() =>
            fail(new Error("Timeout!")),
        2000);

        pc.onicecandidate = (e) => {
            if (!e.candidate) return;

            // Display candidate string e.g
            // candidate:842163049 1 udp 1677729535 XXX.XXX.XX.XXXX 58481 typ srflx raddr 0.0.0.0 rport 0 generation 0 ufrag sXP5 network-cost 999
            console.log(e.candidate.candidate);

            // If a srflx candidate was found, notify that the STUN server works!
            if(e.candidate.type == "srflx"){
                clearTimeout(failTimeout);
                console.log("The STUN server is reachable!");
                console.log(`   Your Public IP Address is: ${e.candidate.address}`);
                resolve();
            }

            // If a relay candidate was found, notify that the TURN server works!
            if(e.candidate.type == "relay"){
                clearTimeout(failTimeout);
                console.log("The TURN server is reachable!");
                resolve();
            }
        };

        // Log errors:
        // Remember that in most of the cases, even if its working, you will find a STUN host lookup received error
        // Chrome tried to look up the IPv6 DNS record for server and got an error in that process. However, it may still be accessible through the IPv4 address
        pc.onicecandidateerror = fail;

        pc.createDataChannel('ourcodeworld-rocks');
        pc.createOffer().then(offer => pc.setLocalDescription(offer));
    });
}

async function main() {
    const [node, arg0, ...argv] = process.argv;
    for (const url of argv) {
        console.log("Testing", url);
        await testIceServers([
            { urls: url }
        ]);
    }
    process.exit(0);
}

main();
