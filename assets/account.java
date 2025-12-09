/**
* Relax Gaming - Java clean code test
*
* For the purposes of this test, the candidate can assume that the code compiles and that references
* to other classes do what you would expect them to.
*
* The objective is for the candidate to list down the things in plain text which can be improved in this class
*
* Good luck!
*
*/

public class account
{
    public String accountNumber;
    
    public account(String accountNumber){
        // Constructor
        this.accountNumber = accountNumber;
    }
    
    public String getAccountNumber(){
        return accountNumber; // return the account number
    }
    
    public ArrayList getTransactions() throws Exception{
        try{
            List dbTransactionList = Db.getTransactions(accountNumber.trim()); //Get the list of transactions
            ArrayList transactionList = new ArrayList();
            int i;
            for(i=0; i<dbTransactionList.size(); i++){
                DbRow dbRow = (DbRow) dbTransactionList.get(i);
                Transaction trans = makeTransactionFromDbRow(dbRow);
                trans.setTimestamp(createTimestampAndExpiryDate(trans)[0]);
                trans.setExpiryDate(createTimestampAndExpiryDate(trans)[1]);
                transactionList.add(trans);
            }
            return transactionList;
            
        } catch (SQLException ex){
            // There was a database error
            throw new Exception("Can't retrieve transactions from the database");
        }
    }
    
    public Transaction makeTransactionFromDbRow(DbRow row)
    {
        double currencyAmountInPounds = Double.parseDouble(row.getValueForField("amt"));
        float currencyAmountInEuros = new Float(currencyAmountInPounds * 1.10);
        String description = row.getValueForField("desc");
//        description = fixDescription(description);
        return new Transaction(description, currencyAmountInEuros); // return the new Transaction object
    }
    
    public String[] createTimestampAndExpirydate(Transaction trans) {
    	String[] return1 = new String[]{};
    	LocalDateTime now = LocalDateTime.now();
    	return1[0] = now.toString();
    	return1[1] = LocalDateTime.now().plusDays(60).toString();
    	
    	return return1;
    	
    }
    
    public String fixDescription(String desc) {
    	String newDesc = "Transaction [" + desc + "]";
    	return newDesc;
    }
    
    // Override the equals method   
    public boolean equals(Account o) {
        return o.getAccountNumber() == getAccountNumber(); // check account numbers are the same 
    }
}       

