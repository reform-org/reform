import { MainTests } from './js/target/scala-3.2.1/webapp-test-fastopt/main.js'
import { assert, expect, test } from 'vitest'

test("test", () => {
    let failures = MainTests.main()
    expect(failures).toBe(0)
})
