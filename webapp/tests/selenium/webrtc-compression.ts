import { parseArgs } from "node:util";
import { By, until } from "selenium-webdriver";
import { Peer } from "./lib.js";

/*
mkdir webrtc-compression
cd webrtc-compression

# repeat this a few times
SELENIUM_BROWSER=chrome node --loader ts-node/esm ../webapp/tests/selenium/webrtc-compression.ts >> ../chrome
SELENIUM_BROWSER=firefox node --loader ts-node/esm ../webapp/tests/selenium/webrtc-compression.ts >> ../firefox

split --lines=1 ../chrome chrome
split --lines=1 ../firefox firefox
# 6.8k files
zstd --train -r . --train-cover -o ../dictionary

echo '{"type":"CompleteSession","descType":"offer","sdp":"v=0\r\no=mozilla...THIS_IS_SDPARTA-99.0 3813135671095471591 0 IN IP4 0.0.0.0\r\ns=-\r\nt=0 0\r\na=sendrecv\r\na=fingerprint:sha-256 35:8A:BF:A8:E5:BD:00:01:13:DE:98:BE:FF:8F:C1:98:89:15:BB:13:EA:01:C0:FA:98:E9:6B:11:2B:EE:30:F9\r\na=group:BUNDLE 0\r\na=ice-options:trickle\r\na=msid-semantic:WMS *\r\nm=application 9 UDP/DTLS/SCTP webrtc-datachannel\r\nc=IN IP4 0.0.0.0\r\na=candidate:0 1 UDP 2122187007 085f9d87-3d47-4cc9-82f6-a0f4c698181f.local 49376 typ host\r\na=candidate:1 1 UDP 2122252543 8e6d001d-b79f-4fd5-8172-14e2a7ecf3b1.local 43207 typ host\r\na=candidate:2 1 TCP 2105458943 085f9d87-3d47-4cc9-82f6-a0f4c698181f.local 9 typ host tcptype active\r\na=candidate:3 1 TCP 2105524479 8e6d001d-b79f-4fd5-8172-14e2a7ecf3b1.local 9 typ host tcptype active\r\na=sendrecv\r\na=end-of-candidates\r\na=ice-pwd:dba0ed7ae6912fbf3209bf8f3bbf0271\r\na=ice-ufrag:b06bdb1b\r\na=mid:0\r\na=setup:actpass\r\na=sctp-port:5000\r\na=max-message-size:1073741823\r\n"}' | zstd -D ../dictionary -19 -o test
cat test | qrencode --8bit -o - -t UTF8
cat test | zstd -d -D ../dictionary -19 -o test-decompressed
*/

let peer = await Peer.create(true)
await peer.driver.get("http://localhost:5173/webrtc-compression")
await peer.driver.wait(until.elementTextIs(await peer.driver.findElement(By.id("webrtc-compression-count")), "400"))
let element = await peer.driver.findElement(By.id("webrtc-compression"))
console.log(await element.getText())
await peer.driver.quit()