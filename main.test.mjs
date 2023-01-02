import { MainTest } from './js/target/scala-3.2.1/webapp-test-fastopt/main.js'
import { assert, expect, test } from 'vitest'

test("test", () => {
    let failures = MainTest.main()
    expect(failures).toBe(0)
})
