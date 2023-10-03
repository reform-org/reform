import readline from "readline";
import bcrypt from "bcrypt";
import { db } from "./utils/db.js";
import { randomUUID } from "crypto";
import inquirer from "inquirer"
import { ClassicUser, SSOUser, User, UserTypes } from "./wss/user.js";

const done = () => {
  process.exit(0);
};

const getAllClassicUsers = async () => (await db.all("SELECT * FROM users WHERE type = 'CLASSIC'")).map(user => new ClassicUser(user.id, user.name))
const getUserChoices = (users: User[]) => users.map(user => ({ name: `${user.name} (${user.displayId}) - ${user.type === UserTypes.SSO ? "sso": "classic"}`, value: user.id }))
const getAllSSOUsers = async () => (await db.all("SELECT * FROM users WHERE type = 'SSO'")).map(user => new SSOUser(user.id, user.name))

const addClassicUser = async () => {
  const existingUsers = await getAllClassicUsers()
  inquirer.prompt([
    {
      type: "input",
      name: "username",
      message: "Please enter a username",
      validate: (input) => !existingUsers.find(p => p.name === input) ? true : `user with name ${input} already exists!`
    },
    {
      type: "password",
      name: "password",
      message: "Please enter a password",
      mask: "*"
    },
    {
      type: "password",
      name: "password2",
      message: "Retype the password",
      mask: "*",
      validate: (password, answers) => password === answers.password ? true : "passwords must be the same!"
    }
  ]).then(answers => {
    bcrypt.genSalt(10, (err, salt) => {
      bcrypt.hash(answers.password, salt, (err, hash) => {
        db.instance.run("INSERT OR REPLACE INTO users (name, id, password, type) VALUES(?, ?, ?, 'CLASSIC')", answers.username, randomUUID(), hash, () => {
          console.info(`Stored user ${answers.username} successfully.`)
          main()
        });
      })
    })
  })
}

const resetPassword = async () => {
  const users = getUserChoices(await getAllClassicUsers())
  inquirer.prompt([
    {
      type: "list",
      name: "userid",
      pageSize: 10,
      message: "Please select a user",
      choices: users
    },
    {
      type: "password",
      name: "password",
      message: "Please enter a password",
      mask: "*"
    },
    {
      type: "password",
      name: "password2",
      message: "Retype the password",
      mask: "*",
      validate: (password, answers) => password === answers.password ? true : "passwords must be the same!"
    }
  ]).then(answers => {
    bcrypt.genSalt(10, (err, salt) => {
      bcrypt.hash(answers.password, salt, (err, hash) => {
        db.instance.run("UPDATE users SET password = ? WHERE id = ?", hash, answers.userid, () => {
          console.info(`Updated user ${users.find(user => user.value === answers.userid).name} successfully.`)
          main()
        });
      })
    })
  })
}

const addSSOUser = async () => {
  const existingUsers = await getAllSSOUsers()
  inquirer.prompt([
    {
      type: "input",
      name: "tuid",
      message: "Please enter a TU ID",
      validate: (input) =>  {
        if(existingUsers.find(p => p.id === input)) return `user with TU ID ${input} already exists!`
        if(!input.match(/^[a-z]{2}\d{2}[a-z]{4}$/gm)) return `the value ${input} is no TU ID!`
        return true
      }
    },
  ]).then(answers => {
    db.instance.run("INSERT OR REPLACE INTO users (id, name, type) VALUES(?, ?, 'SSO')", answers.tuid, null, () => {
      console.info(`Stored user with TU ID ${answers.tuid} successfully.`)
      main()
    });
  })
}

const deleteUser = async () => {
  const users = [...await getAllSSOUsers(), ... await getAllClassicUsers()]
  const choices = getUserChoices(users)
  inquirer.prompt([
    {
      type: "list",
      name: "userid",
      pageSize: 10,
      message: "Please select a user",
      choices: choices
    },
    {
      type: "confirm",
      name: "consent",
      message: (answers) => `Are you sure you want to delete the user ${choices.find(user => user.value === answers.userid).name}?`,
      choices: choices
    },
  ]).then(answers => {
    const user = users.find(user => user.id === answers.userid)
    if (answers.consent === true) {
      db.instance.run("DELETE FROM users WHERE id = ?", answers.userid, () => {
        console.info(`Deleted user with TU ID ${user.id} successfully.`)
        main()
      });
    }else{
      main()
    }
    
  })
}

const listUsers = async () => {
  const users = [...await getAllSSOUsers(), ... await getAllClassicUsers()]
  const choices = getUserChoices(users)
  inquirer.prompt([
    {
      type: "list",
      pageSize: 10,
      name: "userid",
      message: "Find a user:",
      choices: choices,
    }
  ]).then(answers => {
    main()    
  })
}

const main = () => {
  inquirer
    .prompt([
      {
        type: "list",
        name: "program",
        message: "What do you want to do?",
        pageSize: 10,
        loop: false,
        choices: [
          {name: "Add a classic user (username & password)", value: 0},
          {name: "Reset a classic users password", value: 1},
          {name: "Add a SSO user", value: 2},
          {name: "Delete a user", value: 3},
          {name: "Find a user", value: 4},
          new inquirer.Separator(),
          {name: "Abort", value: 5}
        ]
      },
    ])
    .then(async answers => {
      db.init();
      switch (answers.program) {
        case 0: await addClassicUser(); break;
        case 1: await resetPassword(); break;
        case 2: await addSSOUser(); break;
        case 3: await deleteUser(); break;
        case 4: await listUsers(); break;
        case 5: done(); break;
      }
    });
}

main()
