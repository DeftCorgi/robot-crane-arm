import javax.swing.*;



class RobotControl
{
   //declare variables
   private Robot r;
   
   //robot variables
   private int h = 2;
   private int w = 1;
   private int d = 0;
   
   //source variables
   private int sourceCol = 10; //assume 10 for all parts
   private int targetCol = 3; //assume 3 for part A and B will increment at the end of each loop
   private int sourceHt = 12; //assume 12 (4 blocks of 3) for part A and B
   
   //bar variables
   private int clearance; //height needed to clear obstacles
   private int highestObstacle = 0; 
   private int[] heights = {0,0,0,0,0,0,0,0,0};
   
   //block variables
   private int blockHt = 3; //assume 3 for part A and B
   
   //counter variables
   private int loop = 0; //counter for iterations in a loop
   
   public RobotControl(Robot r)
   {
       this.r = r;
   }

   public void control(int barHeights[], int blockHeights[])
   {
	 //controlMechanismForScenarioA(barHeights, blockHeights);
	 //controlMechanismForScenarioB(barHeights, blockHeights);
	 controlMechanismForScenarioC(barHeights, blockHeights);
	 
   }
   
   public void controlMechanismForScenarioA(int barHeights[], int blockHeights[])
   {
	   
	   /*
	    BEGIN
	    	Raise first arm enough so second arm just enough to clear all obstacles
	    		lower second arm if possible to reduce number of moves
	     	Extend 2nd arm to the source column
	     	lower 3rd arm to the top of the next block
	     		do not lower 3rd arm if 2nd arm is already touching the source column
	     	pick up block
	     	raise the block enough to clear any obstacles
	     		raise with the 3rd arm
	     	contract 2nd arm to the first available bar 
	     	drop the block on top of the bar
	     	repeat until there are no more blocks to move
    	END
	    */
	   
	   //initialize robot variables
	   h = 2;
	   w = 1;
	   d = 0;
	   sourceHt = 12; //assume 4 blocks of 3 height are being used for scenario A
	   
	   
	   while(sourceHt != 0){
		   //raise the main arm just above the stack of blocks
		   while(h - 1 < sourceHt) {
			   r.up();
			   h++;
		   }
		   
		   //pick up the next block
		   nextBlock();
		   
		   sourceHt -= 3; //assume blockHt is 3 for part A
		   
		   //raise third arm to clear obstacles
		   while(d != 0 ){
			   r.raise();
			   d--;
		   }
		   
		   //contract second arm to the closest free bar from the left
		   contractToTarget();
		   
		   //lower 2nd arm to drop block on bar
		   while(h-1 != 10) {
			   r.down();
			   h--;
		   }
		   
		   //drop the block
		   r.drop();
		   
		   //move the target one position to the right
		   targetCol++;
	   }
   }
   
   public void controlMechanismForScenarioB(int barHeights[], int blockHeights[])
   {
	   
	   /*
	   BEGIN
   		raise first arm 
			go high enough to avoid obstacles
	   		raise it high enough so the next block will be able to clear any bars 
	   	extend 2nd arm
	   	lower third arm
	   		if sourceHt is lower than robot h
	   	pick up block
	   	raise third arm just enough so everything clears obstacles
	   		raise 2nd arm if there is still not enough clearance
	   	contract 2nd arm to target column
	   	lower the block
	   		check if 2nd arm will collide with obstacles
	   		lower with 2nd arm if possible
	   		if not then lower with 3rd arm
	   	drop block
	   	repeat until there are no more blocks to move
	    END
	    */
	   
	   //initialize robot variables
	   h = 2;
	   w = 1;
	   d = 0;
	   
	   //create an array for the heights of all bars and spaces
	   for(int i = 0; i < barHeights.length; i++) {
		   heights[i+3] = barHeights[i];
	   }
	   
	   while (sourceHt != 0) {
		   //raise first arm
		   raiseFirstArm();
		   
		   //pick up net block
		   nextBlock();
		   
		   //remove block from sourceHt
		   sourceHt -= blockHt;
		   
		   //raise third arm until it can clear all obstacles in the way
		   //find clearance
		   contractClearance();
		   
		   //retract arm to target column
		   contractToTarget();
		   
		   //lower 2ndd arm to drop block if arm doesn't collide with any obstacles
		   boolean canDown = true; //2nd arm has room to use down()
		   for (int i = 0; i < targetCol; i++) { //loop through heights[] to detect if any obstcales will be in the way if we lower
			   if (h-2 < heights[i]) { // if lower the 2nd arm by one collides with anything in heights[]
				   canDown = false;
			   }
		   }
		   
		   if (canDown == true) {
			   //if 2nd arm can lower do it
			   while(h - 1 - blockHt > barHeights[loop]) { //assume blockHt is 3 for part B
				   r.down();
				   h--;
			   }
		   } else { //otherwise lower the third arm
			   while(h - 1 - d - blockHt > barHeights[loop]) {
				   r.lower();
				   d++;
			   }
		   }
		   
		   //drop block
		   r.drop();
		   
		   //update heights array
		   heights[targetCol] += blockHt;
		   
		   //increment counter variables
		   loop++;
		   targetCol++;
	   }
   }
   
   public void controlMechanismForScenarioC(int barHeights[], int blockHeights[])
   {
	   /*
		BEGIN
		    raise first arm 
	   			go high enough to avoid obstacles
	   			raise it high enough so the next block will be able to clear any bars 
		    extend 2nd arm
		    	if the 3rd arm is lowered see if it can clear all obstacles on the way to the source column
		    lower third arm
	   			lower third arm if 2nd arm does not reach the top of the next block
		    pick up block
	   			determine size of block
		    raise third arm enough to clear obstacles
	   			raise all the way up
	   			raise 2nd arm if the block will hit obstacle while retracting
		    retract 2nd arm to target column
	   			target column is 1 for block height 1
	   			2 for block height 2
	   			place on first available bar if block height is 3
		    lower the block
	   			check if 2nd arm will collide with obstacles
	   			lower with 2nd arm if possible
	   			if not then lower with 3rd arm
		    drop block
			repeat until no more blocks to move
	   	END
	   */
	   
	   
	   //initialize robot variables
	   h = 2;
	   w = 1;
	   d = 0;
	   int lgTargetCol = 0;
	   
	   //create an array for the heights of all bars and spaces
	   for(int i = 0; i < barHeights.length; i++) {
		   heights[i+3] = barHeights[i];
	   }
	   
	   //loop through block heights array and add up the elements for source height
	   sourceHt = 0;
	   for (int block : blockHeights) {
		   sourceHt += block;
	   }
	   
	   while (sourceHt != 0) {
		   //Get the block height from the array
		   blockHt = blockHeights[blockHeights.length - 1 - loop];
		   
		   raiseFirstArm();
		   
		   //pick up next block
		   nextBlock();
		   
		   //change target column depending on the block height
		   if (blockHt == 1) {
			   targetCol = 1;
		   } else if(blockHt == 2) {
			   targetCol = 2;
		   } else {
			   targetCol = 3 + lgTargetCol;
			   lgTargetCol++;
		   }
		   
		   //remove block from sourceHt
		   sourceHt -= blockHt;
		   
		   //raise third arm until it can clear all obstacles in the way
		   contractClearance();
		   
		   //contract arm to target column
		   contractToTarget();
		   
		   //lower 2ndd arm to drop block if arm doesn't collide with any obstacles
		   highestObstacle = 0;
		   for (int i = 0; i <= targetCol; i++) { //loop through heights[] to detect if any obstcales will be in the way if we lower
			   if (highestObstacle < heights[i]) { // if lower the 2nd arm by one collides with anything in heights[]
				   highestObstacle = heights[i];
			   }
		   }
		   
		   //lower either the second or third arm
		   while (h - 1 - d - blockHt > heights[targetCol]) {
			   if (h - 1 == highestObstacle) { //if 2nd arm reaches obstacle then lower the 3rd arm isntead
				   r.lower();
				   d++;
			   } else {
				   r.down();
				   h--;
			   }
		   }
		   
		   //drop block
		   r.drop();
		   
		   //update heights array
		   heights[targetCol] += blockHt;
		   
		   //increment counter variables
		   loop++;
		   targetCol++;
	   }
   }
   
   private void nextBlock() { //pick up the next block
	   
	   //extend 2nd arm to sourceCol
	   while(w != sourceCol) {
		   r.extend();
		   w++;
	   }
	   
	  //lower the third arm to meet the closest block
	   while(h - 1 - d > sourceHt) { 
		   r.lower();
		   d++; //update depth
	   }
	   
	   //pick up the block and update the sourceHt
	   r.pick();
   }
   
   private void contractToTarget() { //contract second arm to target column
	   while (w > targetCol) {
		   r.contract();
		   w--;
	   }
   }
   
   private void contractClearance() { //raise the 3rd or second arm to clear any obstacles on the way to target column
	   int raiseAmt = 0;
	   for(int i = targetCol; i < heights.length ; i++) { //loop through the columsn which the block will be dragged over and get highest obstacle
		   if (raiseAmt < heights[i]) {
			   raiseAmt = heights[i];
		   }
	   }
	   
	   int effHt = (h - 1 - d - blockHt); //effective height
	   while (effHt < raiseAmt) {
		   System.out.println(blockHt);
		   System.out.println(raiseAmt);
		   System.out.println("------");
		   if(d == 0) { //if depth is 0 then raise the second arm
			   r.up();
			   effHt++;
			   h++;
		   } else {
			   r.raise();//otherwise raise the third arm 
			   effHt++;
			   d--;
		   }
	   }
   }
   
   //raise the first arm so that the second arm won't run into any obstacles while extending
   private void raiseFirstArm() {
	 //see if block while be dragged into any obstacles when we extend 2nd arm
	   highestObstacle = 0;
	   for(int i=targetCol;i < heights.length;i++) {
		   if (highestObstacle < heights[i]) {
			   highestObstacle = heights[i];
		   }
	   }
	   
	   //highestObstacle += blockHt; //take into consideration the blockHt
	   //calculate if the obstacles or sourceHt is higher
	   if(sourceHt < highestObstacle) {
		   clearance = highestObstacle;
	   } else {
		   clearance = sourceHt;
	   }
	   
	   //raise first arm
	   while (h-1 < clearance) { //go high enough to avoid obstacles
		   r.up();
		   h++;
	   }
   }
} 


