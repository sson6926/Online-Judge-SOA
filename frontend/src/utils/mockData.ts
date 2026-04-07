export type Difficulty = 'Easy' | 'Medium' | 'Hard';
export type Status =
'Accepted' |
'Wrong Answer' |
'Time Limit Exceeded' |
'Runtime Error' |
'Compile Error' |
'Pending';

export interface Problem {
  id: string;
  title: string;
  difficulty: Difficulty;
  acceptance: string;
  description: string;
  examples: {input: string;output: string;explanation?: string;}[];
  constraints: string[];
}

export interface Submission {
  id: string;
  userId: string;
  username: string;
  problemId: string;
  problemTitle: string;
  language: string;
  status: Status;
  runtime: string;
  memory: string;
  timestamp: string;
}

export const MOCK_PROBLEMS: Problem[] = [
{
  id: '1',
  title: 'Two Sum',
  difficulty: 'Easy',
  acceptance: '49.2%',
  description:
  'Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.\n\nYou can return the answer in any order.',
  examples: [
  {
    input: 'nums = [2,7,11,15], target = 9',
    output: '[0,1]',
    explanation: 'Because nums[0] + nums[1] == 9, we return [0, 1].'
  },
  { input: 'nums = [3,2,4], target = 6', output: '[1,2]' }],

  constraints: [
  '2 <= nums.length <= 10^4',
  '-10^9 <= nums[i] <= 10^9',
  '-10^9 <= target <= 10^9',
  'Only one valid answer exists.']

},
{
  id: '2',
  title: 'Add Two Numbers',
  difficulty: 'Medium',
  acceptance: '40.1%',
  description:
  'You are given two non-empty linked lists representing two non-negative integers. The digits are stored in reverse order, and each of their nodes contains a single digit. Add the two numbers and return the sum as a linked list.',
  examples: [{ input: 'l1 = [2,4,3], l2 = [5,6,4]', output: '[7,0,8]' }],
  constraints: [
  'The number of nodes in each linked list is in the range [1, 100].']

},
{
  id: '3',
  title: 'Longest Substring Without Repeating Characters',
  difficulty: 'Medium',
  acceptance: '33.8%',
  description:
  'Given a string `s`, find the length of the longest substring without repeating characters.',
  examples: [
  {
    input: 's = "abcabcbb"',
    output: '3',
    explanation: 'The answer is "abc", with the length of 3.'
  }],

  constraints: [
  '0 <= s.length <= 5 * 10^4',
  's consists of English letters, digits, symbols and spaces.']

},
{
  id: '4',
  title: 'Median of Two Sorted Arrays',
  difficulty: 'Hard',
  acceptance: '36.0%',
  description: '...',
  examples: [],
  constraints: []
},
{
  id: '5',
  title: 'Longest Palindromic Substring',
  difficulty: 'Medium',
  acceptance: '32.4%',
  description: '...',
  examples: [],
  constraints: []
},
{
  id: '20',
  title: 'Valid Parentheses',
  difficulty: 'Easy',
  acceptance: '40.2%',
  description: '...',
  examples: [],
  constraints: []
},
{
  id: '21',
  title: 'Merge Two Sorted Lists',
  difficulty: 'Easy',
  acceptance: '62.4%',
  description: '...',
  examples: [],
  constraints: []
},
{
  id: '42',
  title: 'Trapping Rain Water',
  difficulty: 'Hard',
  acceptance: '59.1%',
  description: '...',
  examples: [],
  constraints: []
},
{
  id: '56',
  title: 'Merge Intervals',
  difficulty: 'Medium',
  acceptance: '46.0%',
  description: '...',
  examples: [],
  constraints: []
},
{
  id: '146',
  title: 'LRU Cache',
  difficulty: 'Medium',
  acceptance: '40.8%',
  description: '...',
  examples: [],
  constraints: []
}];


export const MOCK_SUBMISSIONS: Submission[] = [
{
  id: 's1',
  userId: 'u1',
  username: 'alex_dev',
  problemId: '1',
  problemTitle: 'Two Sum',
  language: 'Python',
  status: 'Accepted',
  runtime: '45 ms',
  memory: '16.2 MB',
  timestamp: '2 mins ago'
},
{
  id: 's2',
  userId: 'u2',
  username: 'coder99',
  problemId: '3',
  problemTitle: 'Longest Substring...',
  language: 'C++',
  status: 'Wrong Answer',
  runtime: 'N/A',
  memory: 'N/A',
  timestamp: '5 mins ago'
},
{
  id: 's3',
  userId: 'u3',
  username: 'algo_master',
  problemId: '42',
  problemTitle: 'Trapping Rain Water',
  language: 'Java',
  status: 'Time Limit Exceeded',
  runtime: 'N/A',
  memory: 'N/A',
  timestamp: '12 mins ago'
},
{
  id: 's4',
  userId: 'u1',
  username: 'alex_dev',
  problemId: '1',
  problemTitle: 'Two Sum',
  language: 'Python',
  status: 'Runtime Error',
  runtime: 'N/A',
  memory: 'N/A',
  timestamp: '15 mins ago'
},
{
  id: 's5',
  userId: 'u4',
  username: 'jessica_w',
  problemId: '20',
  problemTitle: 'Valid Parentheses',
  language: 'JavaScript',
  status: 'Accepted',
  runtime: '52 ms',
  memory: '42.1 MB',
  timestamp: '1 hour ago'
},
{
  id: 's6',
  userId: 'u5',
  username: 'byte_me',
  problemId: '146',
  problemTitle: 'LRU Cache',
  language: 'Go',
  status: 'Compile Error',
  runtime: 'N/A',
  memory: 'N/A',
  timestamp: '2 hours ago'
}];