package com.broada.carrier.monitor.impl.db.oracle.recursion;

public class OracleRecursionInfo {
  private long recursion_call_num;// 递归调用数
  
  private long user_call_num;// 用户调用数

  public void setRecursion_call_num(long recursion_call_num) {
    this.recursion_call_num = recursion_call_num;
  }

  public void setUser_call_num(long user_call_num) {
    this.user_call_num = user_call_num;
  }
  
  public long getRecursion_call_num() {
    return recursion_call_num;
  }

  public long getUser_call_num() {
    return user_call_num;
  }

  /**
   * 获取时间间隔内递归调用百分比
   * 
   * @param lastRecursionCallNum 上次monitor的递归调用数
   * @param lastUserCallNum 上次monitor的用户调用数
   * @return
   */
  public double getRecursionCallRatioInTimes(double lastRecursionCallNum, double lastUserCallNum){
    if(lastRecursionCallNum == 0 || lastUserCallNum == 0){
      return 0;
    }
    double newRecursionCallNum = (double)recursion_call_num - lastRecursionCallNum;
    double total = newRecursionCallNum + (double)user_call_num - lastUserCallNum;
    if(total == 0){
      return 0;
    }
    return (double)newRecursionCallNum/total * 100;
  }
  
  /**
   * 获取递归调用百分比
   * 
   * @return
   */
  public double getRecursionCallRatio(){
    long total = recursion_call_num + user_call_num;
    if(total == 0){
      return 0;
    }
    return (double)recursion_call_num/total * 100;
  }
  
  /**
   * 获取递归调用速率
   * 
   * @param lastRecursionCallNum 上次monitor的递归调用数
   * @param seconds 时间间隔(秒)
   * @return
   */
  public double getRecursionCallVelocity(double lastRecursionCallNum, double seconds){
    if(seconds == 0 || lastRecursionCallNum == 0){
      return 0;
    }
    double newRecursionCallNum = (double)recursion_call_num - lastRecursionCallNum;
    return newRecursionCallNum/seconds;
  }
  
  /**
   * 获取递归——用户调用比率
   * 
   * @return
   */
  public double getRecursionUserRatio(){
    if(user_call_num == 0){
      return 0;
    }
    return (double)recursion_call_num/user_call_num;
  }
}
