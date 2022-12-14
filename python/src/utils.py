import os
from typing import List, Union


def get_input(day: int) -> str:
    base_path = "../../inputs"
    path = os.path.join(base_path, f"day{day}.in")
    with open(path, 'r') as f:
        return "".join(f.readlines())


def get_output(day: int) -> List[Union[int, str]]:
    base_path = "../../outputs"
    path = os.path.join(base_path, f"day{day}.out")
    with open(path, 'r') as f:
        lines = f.readlines()
        if len(lines) > 2:
            return [intify_maybe(lines[0]), "".join(lines[1:])]
        else:
            return list(map(intify_maybe, lines))


def intify_maybe(maybe_int: str) -> Union[int, str]:
    try:
        return int(maybe_int)
    except:
        return maybe_int
