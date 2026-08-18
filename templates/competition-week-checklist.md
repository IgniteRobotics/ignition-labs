### Before leaving for the event

- [ ] Everything committed and pushed. `git status` is clean
- [ ] `main` builds: `./gradlew build`
- [ ] Tag it: `git tag pre-[event-name] && git push --tags`
- [ ] Confirm the code on the robot matches `main`
- [ ] At least two people have the repo cloned and can deploy

### Every day at the event

- [ ] Morning: confirm what is deployed matches a known commit
- [ ] **Every change made in the pit gets committed.** Even a bad message beats an uncommitted change
- [ ] Before each match: is deployed code the code you think it is?
- [ ] End of day: push everything

### After the event

- [ ] Tag it: `git tag post-[event-name] && git push --tags`
- [ ] Write down what broke and what you changed — feeds the retro
