# scala-coco-scheduler

A small Scala 3 command-line tool that parses cron expressions and prints the possible values for each field.

## Run

```bash
sbt "run --cron '*/15 0 1,15 * 1-5'"
```

## Test

```bash
sbt test
```

## Supported cron syntax

- `*` — wildcard
- `*/n` — step values
- `a-b` — ranges
- `a,b` — lists
- single numbers

Fields are expected in the order: minute, hour, day of month, month, weekday.
