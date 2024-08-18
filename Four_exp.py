'''
#CREATING LIST
fruits = ["apple", "orange", "mango", "strawberry", "watermelon", "grapes"]
fruit = fruits[3]
print ("The fruit in index 2 is :"+fruit)
'''
'''
#UPDATE LIST
fruits = ["apple", "orange", "mango", "strawberry", "watermelon", "grapes"]
fruits [1]="pineapple"
print ("The updated index is :"+fruits [1])
'''
'''
#DELETE LIST
fruits = ["apple", "orange", "mango", "strawberry", "watermelon", "grapes"]
del fruits [1]
print ("The list after deletion :",fruits )
'''
'''
#IN-BUILT FUNCTIONS IN LIST
numberList = [1,2,3,4,5,6,7,8,9,12,43,57,87,98,3,True] 
length = len(numberList) # this find length of list 
print("LENGTH OF LIST", length)
list_append = numberList.append(4) # this add element in last of list 
print("APPEND LIST",numberList)
other_list=[23,63,89]
numberList.extend(other_list) # this method add other list to our main list 
print("EXTEND LIST : ", numberList)
numberList.insert(1, 10) # Insert 10 at index 1 
print("INSERTING THE ELEMENT", numberList)
numberList.remove(3) # Removes the first occurrence of 3 
print("REMOVING THE ELEMENT",numberList)
popped_element = numberList.pop(1) # Removes and returns the element at index 1 
print("REMOVE AND RETURN THE INDEX:", numberList)
index = numberList.index(3) # Returns the index of the first occurrence of 3 
print("RETURN INDEX OF FIRST OCCUURENCE : ", index)
count = numberList.count(3) # Returns the number of occurrences of 3 
print("COUNT OF OCCURENCE:", count)
numberList.sort()
print("SORTING IN ASCENDING ORDER : ", numberList)
numberList.reverse()
print("REVERSE THE STRING", numberList)
print(min(numberList)) #Returns the minimum number in the list
print(any(numberList)) #Returns true if any of the elements in the list is true,otherwise false
print(all(numberList)) #Returns true if all the elements of list are true
'''