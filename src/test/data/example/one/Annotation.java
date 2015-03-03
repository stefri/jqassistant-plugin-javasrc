package example.one;

public @interface Optimize
{
  enum Priority { LOW, NORM, HIGH }
  String   value();
  String[] assignedTo()   default ""  ;
  Priority priority()     default Priority.NORM  ;
}
