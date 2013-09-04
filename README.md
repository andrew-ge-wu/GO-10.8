## Greenfield Online

This repository contains the Greenfield Online example project.

### Branches

The Greenfield Online repository contains two types of branches, prefixed 'RELEASE'/'DEV-RELEASE' and 'RELENG'.

#### RELEASE branches

RELEASE branches (and DEV-RELEASE branches) contain released versions of Greenfield Online. Code on these branches depends on released versions of Polopoly.

#### RELENG branches

RELENG branches contain the latest Greenfield Online code for a particular version. Code on these branches usually depends on unreleased versions of Polopoly (using SNAPSHOT dependencies). At this time Polopoly does not distribute SNAPSHOT versions of our products, meaning it is not possible to build RELENG branches. It is still possible to use these branches to track changes.

### Issue handling

All Greenfield Online issue and support handling is performed in the [Greenfield Online](http://support.polopoly.com/jira/browse/GO "Greenfield Online") JIRA space on the support site.

### Contact

For information on the Greenfield Online repository, contact Polopoly Support at support.polopoly@atex.com.

## Pull requests

Pull requests to the Greenfield Online repository is a good way to contribute changes and enhancements. However, please follow these guidelines when making changes to Greenfield Online that you want to have committed back to the master branch:

### 1. Fork the repository

Fork the Greenfield Online repository at either `https://github.com/atex-polopoly/greenfield-online` or `git@github.com:atex-polopoly/greenfield-online.git` (the latter one require that you've set up your GitHub SSH keys).

### 2. Make a branch for the fix

In order to not pollute your entire Greenfield Online fork for this specific fix, make a new branch for it.

Polopoly does not publish SNAPSHOT builds, which means you will not be able to build the master branch or any RELENG branch. Therefore, this fix-branch should be based on the *latest* release-branch available in Greenfield Online (such as DEV-RELEASE-10-7-1 or RELEASE-10-6-1).

For example, if you want to fix a bug in the Integration Server and the latest release-branch is DEV-RELEASE-10-7-1:

```
git push origin DEV-RELEASE-10-7-1:integration-server-crash-fix
git checkout -t origin/integration-server-crash-fix
```

### 3. Make your changes

Make your changes to Greenfield Online on the branch you now have checked out. When you are finished, make sure that everything builds correctly and that all tests pass (run a full 'mvn clean install -P run-acceptance-tests' in the Greenfield Online root). Make sure your changes are committed.
 
### 4. Create a JIRA issue

When you are ready to make a pull request, create a JIRA issue for it in the [Greenfield Online](http://support.polopoly.com/jira/browse/GO "Greenfield Online") JIRA project. It doesn't matter very much what you name the issue, just make sure to describe that you want to make a pull request. Don't set any fix version or component for the issue. Assign the issue to yourself if possible.

### 5. Prepare the pull request

Create a new branch based on the master branch, and cherry-pick over the changes you want to be included:

```
# The following will make sure the master branch of your fork is up to date. This is important because
# we want all pull requests to be made from the master branch, and we want you to solve any merge
# conflicts against the latest state of the master branch.

git remote add GO https://github.com/atex-polopoly/greenfield-online.git

git fetch GO

git checkout master
git merge GO/master

git push

# The following will create a branch 'pull-request-13' (in your forked repository) based on the now up to
# date master branch.
 
git push origin master:pull-request-13

git checkout -t origin/pull-request-13

# The following replicates your changes to the pull request branch (which is based on the master branch)
# and commits them as a single changeset.

git cherry-pick [hash #1] -n
git cherry-pick [hash #2] -n
...
git cherry-pick [hash #n] -n
 
git commit -m "GO-YYY: The Integration Server should no longer crash."
 
git push
```

### 6. Make the pull request

Now make the pull request on GitHub. Make sure the pull request is FROM [myaccount]:greenfield-online:[branch] TO atex-polopoly:greenfield-online:master.

### What happens now?

Given that the changes contained in the pull request are deemed to be good, what will now happen (on the Polopoly Products side) is:

#### 1. Code review

Everything changed in the pull request will be reviewed. Minor errors will be fixed, but anything major will be reported back in the pull request discussion instead. It will then have to be fixed by the person making the pull request.

#### 2. Test jobs

Everything changed in the pull request will be run through test jobs, ensuring nothing obvious is broken. Minor errors will be fixed, but anything major will be reported back in the pull request discussion instead. It will then have to be fixed by the person making the pull request.

#### 3. Merge to master

If everything in (1) and (2) works out, the pull request will be merged to the master Greenfield Online repository.

#### 4. Closed pull request

The pull request will be closed with information on when and where the changes of the pull request will appear. Note that due to technical details concerning how the Greenfield Online repository is currently set up, the actual merge to the master branch is performed "behind the scenes" by Polopoly Products. The pull request will not be "accepted", but instead closed with information on where and in which Greenfield Online version the changes will appear.

## Code Status
The code in this repository is provided with the following status: **EXAMPLE**.

Under the open source initiative, Atex provides source code for plugin with different levels of support. There are three different levels of support used. These are:

- EXAMPLE
The code is provided as an illustration of a pattern or blueprint for how to use a specific feature. Code provided as is.

- PROJECT
The code has been identified in an implementation project to be generic enough to be useful also in other projects. This means that it has actually been used in production somewhere, but it comes "as is", with no support attached. The idea is to promote code reuse and to provide a convenient starting point for customization if needed.

- PRODUCT
The code is provided with full product support, just as the core Polopoly product itself.
If you modify the code (outside of configuraton files), the support is voided.

## License
Atex Polopoly Source Code License
Version 1.0 February 2012

See file **LICENSE** for details.

