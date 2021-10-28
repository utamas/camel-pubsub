First start the environment.

Make sure that `31085` (for pubsub emulator) and `32100` (for redis) are available.

```shell
terraform apply -auto-approve 
```

Then run `DemoApplication` with `camel.component.google-pubsub.authenticate=false` and `camel.component.google-pubsub.endpoint=localhost:31085`.

Now you are ready to make calls to the API.

```shell
curl localhost:8080/kicks
```

should yield something like:

```
data:987a36f5-435a-4923-9ef2-c19d45c49302

data:{"correlationId":"987a36f5-435a-4923-9ef2-c19d45c49302","message":"Hi. My time is: 2021-10-28T12:28:25.856881"}
```