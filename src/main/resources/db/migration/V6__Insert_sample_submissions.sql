INSERT IGNORE INTO code_submissions (
    code,
    language,
    output,
    error,
    status,
    execution_time,
    memory_used,
    is_correct,
    user_id,
    problem_id,
    submitted_at
) VALUES
(
    'public class Solution {
    public int[] twoSum(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{};
    }

    public static void main(String[] args) {
        Solution sol = new Solution();
        int[] result = sol.twoSum(new int[]{2,7,11,15}, 9);
        System.out.println("[" + result[0] + "," + result[1] + "]");
    }
}',
    'java',
    '[0,1]',
    NULL,
    'SUCCESS',
    150.5,
    45.2,
    true,
    (SELECT id FROM users WHERE username = 'Sanjeev'),
    (SELECT id FROM problems WHERE title = 'Two Sum'),
    NOW() - INTERVAL 2 DAY
),
(
    'def reverse_string(s):
    return s[::-1]

print(reverse_string("hello"))',
    'python',
    'olleh',
    NULL,
    'SUCCESS',
    50.2,
    25.1,
    true,
    (SELECT id FROM users WHERE username = 'Sanju'),
    (SELECT id FROM problems WHERE title = 'Reverse String'),
    NOW() - INTERVAL 1 DAY
),
(
    'function isPalindrome(s) {
    return s === s.split("").reverse().join("");
}

console.log(isPalindrome("racecar"));',
    'javascript',
    'true',
    NULL,
    'SUCCESS',
    75.8,
    30.5,
    true,
    (SELECT id FROM users WHERE username = 'Aditya'),
    (SELECT id FROM problems WHERE title = 'Palindrome Check'),
    NOW() - INTERVAL 3 HOUR
),
(
    '#include <iostream>
#include <vector>
using namespace std;

int fibonacci(int n) {
    if (n <= 1) return n;
    return fibonacci(n-1) + fibonacci(n-2);
}

int main() {
    cout << fibonacci(5);
    return 0;
}',
    'cpp',
    '5',
    NULL,
    'SUCCESS',
    200.1,
    60.3,
    true,
    (SELECT id FROM users WHERE username = 'Sahil'),
    (SELECT id FROM problems WHERE title = 'Fibonacci Number'),
    NOW() - INTERVAL 5 HOUR
),
(
    'package main
import "fmt"

func binarySearch(nums []int, target int) int {
    left, right := 0, len(nums)-1
    for left <= right {
        mid := left + (right-left)/2
        if nums[mid] == target {
            return mid
        } else if nums[mid] < target {
            left = mid + 1
        } else {
            right = mid - 1
        }
    }
    return -1
}

func main() {
    nums := []int{-1,0,3,5,9,12}
    fmt.Print(binarySearch(nums, 9))
}',
    'go',
    '4',
    NULL,
    'SUCCESS',
    120.7,
    40.8,
    true,
    (SELECT id FROM users WHERE username = 'Deepak'),
    (SELECT id FROM problems WHERE title = 'Binary Search'),
    NOW() - INTERVAL 1 HOUR
),
(
    'public class InvalidCode {
    public static void main(String[] args) {
        System.out.println("Hello World"
    }
}',
    'java',
    NULL,
    'Compilation error: missing semicolon',
    'ERROR',
    0.0,
    0.0,
    false,
    (SELECT id FROM users WHERE username = 'Sanjeev'),
    (SELECT id FROM problems WHERE title = 'Two Sum'),
    NOW() - INTERVAL 30 MINUTE
);