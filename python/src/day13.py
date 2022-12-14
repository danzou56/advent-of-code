import textwrap
from functools import cmp_to_key

from utils import get_output, get_input


data = get_input(13)
expected = get_output(13)


def test_example():
    data = textwrap.dedent("""\
        [1,1,3,1,1]
        [1,1,5,1,1]
        
        [[1],[2,3,4]]
        [[1],4]
        
        [9]
        [[8,7,6]]
        
        [[4,4],4,4]
        [[4,4],4,4,4]
        
        [7,7,7,7]
        [7,7,7]
        
        []
        [3]
        
        [[[]]]
        [[]]
        
        [1,[2,[3,[4,[5,6,7]]]],8,9]
        [1,[2,[3,[4,[5,6,0]]]],8,9]""")

    assert part1(data) == 13
    assert part2(data) == 140


def test_part1():
    actual = part1(data)
    print(f"Actual: {actual}")
    assert actual == expected[0], \
        f"Expected {expected[0]} but actually was {actual}"


def test_part2():
    actual = part2(data)
    print(f"Actual: {actual}")
    assert actual == expected[1], \
        f"Expected {expected[1]} but actually was {actual}"


def part1(input: str) -> int:
    pairs = [pair.split("\n") for pair in input.split("\n\n")]
    list_pairs = [[eval(pair[0]), eval(pair[1])] for pair in pairs]

    in_order_pairs_indices = sum([
        i + 1 if in_right_order(pair[0], pair[1]) else 0
        for i, pair in enumerate(list_pairs)
    ])

    return in_order_pairs_indices


def part2(input: str):
    packets = sorted([eval(line) for line in input.split("\n") if line] + [[[2]], [[6]]], key=cmp_to_key(compare))
    index_of_2 = packets.index([[2]]) + 1
    index_of_6 = packets.index([[6]]) + 1
    return index_of_2 * index_of_6


def compare(left, right) -> int:
    comparison = in_right_order(left, right)
    assert comparison is not None, \
        "Comparison returned `None` which should not occur"

    return -1 if comparison else 1


def in_right_order(left, right) -> bool:
    """
    Returns True if the left comes before right
    :param left:
    :param right:
    :return:
    """
    # print(f"Comparing {left} with {right}")
    if len(left) == 0 and len(right) == 0:
        return None
    if len(left) == 0:
        return True
    if len(right) == 0:
        return False

    left_val = left[0]
    right_val = right[0]

    if isinstance(left_val, int) and isinstance(right_val, int):
        if left_val > right_val:
            return False
        if left_val < right_val:
            return True
        return in_right_order(left[1:], right[1:])
    if isinstance(left_val, int) and not isinstance(right_val, int):
        left_val = [left_val]
    elif not isinstance(left_val, int) and isinstance(right_val, int):
        right_val = [right_val]

    assert not isinstance(left_val, int)
    assert not isinstance(right_val, int)

    ret = in_right_order(left_val, right_val)
    if ret is None:
        return in_right_order(left[1:], right[1:])
    else:
        return ret



