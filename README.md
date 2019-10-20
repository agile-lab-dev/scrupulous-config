# ScrupulousConfig

ScrupulousConfig is a command line tool and a library that compares two 
[lightbend config](https://github.com/lightbend/config) and outputs
a human-readable diff. 

ScrupulousConfig is exitcode "friendly" this means that it returns 0 as a exit code
if and only if the two configurations are semantically the same.

## Quickstart

1. Clone the repository
1. Build the project `sbt clean assembly`
1. Run the tool to generate your current resolved config: `java -cp "scrupulous-config-assembly-0.1.0.jar:<your-application-artifacts>" it.agilelab.utils.scrupulous.ScrupulousConfig -t template.conf -c application.conf -o`
1. Perform changes at `application.conf`
1. Re-run the tool without `overwrite` flag: `java -cp "scrupulous-config-assembly-0.1.0.jar:<your-application-artifacts>" it.agilelab.utils.scrupulous.ScrupulousConfig -t template.conf -c application.conf`
1. Check the output and evaluate if the changes are what you expect or you have any unwanted changes reach your application config!

## Usage

```
Usage: ScrupulousConfig --template <path> --config <path> [--defaultValue <key=value>]... [--pathToIgnore <string>]... [--verbose] [--overwrite]

Outputs diffs between a rendered (template) lightbend config and another config still to be resolved. The template one does not get resolved, only parsed, while the second one is resolved (i.e. through reference.conf files found in the classpath and so on, like in your regular final application).

Options and flags:
    --help
        Display this help text.
    --template <path>, -t <path>
        The template conf to parse
    --config <path>, -c <path>
        The conf to resolve
    --defaultValue <key=value>, -d <key=value>
        Default values to be injected in the resolved config. The format must be key=value. Only unquoted strings are supported
    --pathToIgnore <string>, -i <string>
        Config paths that have to be ignored during comparison
    --verbose, -v
        Verbose mode
    --overwrite, -o
        Overwrite the template with the resolved conf
```

## Motivation

Developers are used to test code they write, developers love to make things configurable and 
we at [Agilelab](www.agilelab.it) make an extensive use of configurations 
and in particular [lightbend config](https://github.com/lightbend/config).

We also end up composing multiple configuration files in order to avoid 
repeated configuration keys that are cumbersome to keep aligned.

For the "runnable" part of our artifacts (i.e. scala code) we configure and run unit and
integration tests that insure that the behavior of the applications we develop is stable and
as expected. The same attention is not given to configuration of applications, and that is a shame!

Therefore we developed this tool to make easy to resolve and then compare a template configuration 
against the "current" configuration that we are going to deploy. This means that even a small change 
inside `reference.conf` inside an artifact will be caught and highlighted to whoever is responsible of
deploying a new version of the application. 


## Technical details

ScrupulousConfig is an attempt to achieve something useful while learning something new 
(I encourage everyone to do the same from time to time). 

Therefore we started developing it using the "Typelevel stack": [cats](https://typelevel.org/cats/) 
and [cats-effect](https://typelevel.org/cats-effect/).

To manage CLI arguments we picked [monovore decline](http://ben.kirw.in/decline/).

To perform the actual semantic diff between the two configs we picked [Gnieh Diffson](https://github.com/gnieh/diffson) with spray json as a backend.

Finally to test we tried out [zio-test](http://github.com/zio/zio).