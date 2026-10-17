# Assignment 4 submission checklist

## Prepared locally

- [x] Suggested Option B topic, so free-topic approval is unnecessary.
- [x] Five public, stateful subsystem classes with actual validation and interactions.
- [x] One facade with two public operations and private device references.
- [x] Ordinary client imports and calls only the facade.
- [x] No subsystem references to the facade; direct use demonstrated by a test.
- [x] Java 17 compatibility compilation with warnings treated as errors.
- [x] Demo ran locally and 31 local behavioral scenarios passed.
- [x] README, source structure, automated checks, and incremental local Git history.
- [x] UML class diagram and editable diagram source.
- [x] Report introduction, UML, six annotated Clean Code examples, and conclusion.
- [x] Defense walkthrough and questions prepared.

## Required before submission

- [ ] Publish this repository's existing local Git history to the correct GitHub account.
- [ ] Replace `GITHUB_REPOSITORY_URL` in report Section 5 with the actual repository link.
- [ ] Open the link while signed in as the instructor would, or grant access if private.
- [ ] Check the GitHub Actions result after pushing. Its configuration targets JDK 17;
      local execution used JDK 25.0.2 with `--release 17`.
- [ ] Add your name and group to the report if your instructor expects them.
- [ ] Read and understand the code and report; follow the assignment's individual-work policy.
- [ ] Confirm the actual Week 6 Sunday deadline in the Moodle course calendar.
      The instructions do not establish an exact calendar date.
- [ ] Upload the report and GitHub link to Moodle before that deadline.
- [ ] Confirm the upload is accepted and rehearse for the last practice class of Week 7.

## Publish the prepared history

Create an empty repository in your own GitHub account without a README, license,
or `.gitignore`. Open a terminal in this project folder. Replace the sample URL
with that repository's actual URL:

```sh
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
git push -u origin main
```

Do not upload only a ZIP through the GitHub web editor: that would omit the
incremental history already prepared here. The downloadable submission ZIP
includes the local `.git` directory so that the existing commits are preserved.

## Grading reminder

Report: 10 percent. Working and Clean Code: 40 percent. Defense: 50 percent.
Local checks do not predict a guaranteed grade or replace the instructor's rubric.
