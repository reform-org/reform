import { MainTest } from './js/target/scala-3.2.2/webapp-test-fastopt/main.js'
import { assert, expect, test } from 'vitest'

// https://github.com/search?q=repo%3Ascala-js%2Fscala-js+scalajsCom
// https://github.com/scala-js/scala-js/issues/2224
// https://github.com/scala-js/scala-js/blob/c0e07692eb25c3cc3ab2319bc64f2233e1c3b910/test-bridge/src/main/scala/org/scalajs/testing/bridge/JSRPC.scala#L30
// if we replace indexeddb and maybe switch back to scalajs-bundler we could run the tests in node?

test("test", () => {
    let failures = MainTest.main()
    expect(failures).toBe(0)
})
