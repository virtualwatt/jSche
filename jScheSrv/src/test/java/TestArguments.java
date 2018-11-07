
public class TestArguments {

	public static void main(String[] args) {
		if (args.length == 0)
			System.out.println("No arguments given");
		else
			System.out.println("Following arguments are given: ");
		for (String arg: args)
			System.out.println(arg);
	}

}
